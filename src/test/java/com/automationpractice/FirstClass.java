package com.automationpractice;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.IOException;
import java.util.List;

@RunWith(JUnitParamsRunner.class)
public class FirstClass {

    public ChromeDriver driver;

    @Before
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\pislyakov\\Downloads\\chromedriver.exe");
        driver = new ChromeDriver();

    }

    @Test
    @Parameters({"Summer", "Dress", "t-shirt"})
    public void searchTest(String searchText) throws InterruptedException, IOException {

        // 1. открываем сайт http://automationpractice.com/
        driver.get("http://automationpractice.com/index.php");

        // 2. в поле поиска вводим ключевое слово: 'Summer' и нажимаем значок поиска (лупу)
        WebElement element = driver.findElement(By.xpath("//*[@id=\"search_query_top\"]"));
        element.click();
        element.sendKeys(searchText);
        driver.findElement(By.xpath("//*[@name=\"submit_search\"]")).click();
        WebElement dynamicElement = (new WebDriverWait(driver, 10))
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class=\"lighter\"]")));

        // 3. проверяем, что над списком продуктов в надписи 'SEARCH' отображается наш поисковый запрос - "SUMMER"
        String expectedSearchText = driver.findElement(By.xpath("//*[@class=\"lighter\"]")).getText().substring(1);
        expectedSearchText = expectedSearchText.substring(0, expectedSearchText.length() - 1);
        Assert.assertEquals(searchText.toUpperCase(), expectedSearchText);

        // 4. открываем дропдаун сортировки и выбираем опцию 'Price: Highest first'
        WebElement dynamicDropDown = (new WebDriverWait(driver, 20))
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"selectProductSort\"]")));
        driver.findElement(By.xpath("//*[@id=\"selectProductSort\"]")).click();
        driver.findElement(By.xpath("//*[@id=\"selectProductSort\"]/option[contains(text(),\"Price: Highest first\")]")).click();

        // 5. проверяем, что элементы отсортированы в соответствии с выбранной опцией (сейчас сортировка идёт по старой цене - если у товара есть скидка, нужно смотреть на старую цену)
        List<WebElement> contentBlockList = driver.findElements(By.xpath("//*[@class=\"right-block\"]//*[@class=\"content_price\"]"));
        String allPricesInTheBlock;
        float productPrice;
        float temp = 1000000;
        for (WebElement webElement : contentBlockList) {
            allPricesInTheBlock = webElement.getText();
            if (allPricesInTheBlock.startsWith("$", 7)) {
                productPrice = Float.parseFloat(allPricesInTheBlock.substring(8, 13));
            } else {
                productPrice = Float.parseFloat(allPricesInTheBlock.substring(1, 6));
            }
            Assert.assertTrue(productPrice <= temp);
            temp = productPrice;
        }

        // 6. берем первый из найденных товаров и запоминаем его полное название и цену
        WebElement dynamicProductName = (new WebDriverWait(driver, 20))
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"center_column\"]//*[@class=\"product-name\"]")));
        String firstProductFullName = driver.findElement(By.xpath("//*[@id=\"center_column\"]//*[@class=\"product-name\"]")).getText();
        String firstProductPrice = driver.findElement(By.xpath("//*[@class=\"right-block\"]//*[@class=\"price product-price\"]")).getText();
        System.out.println("Название " + firstProductFullName + " Цена " + firstProductPrice);

        // 7. добавляем его в корзину
        driver.findElement(By.xpath("//*[@class=\"button ajax_add_to_cart_button btn btn-default\"]")).click();

        // 8. открываем корзину и сравниваем название и цену в колонке "Total" у товара, на соответствие с сохраненными значениями
        WebElement dynamicButton = (new WebDriverWait(driver, 20))
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@title=\"Close window\"]")));
        Thread.sleep(5000);
        //driver.findElement(By.xpath("//*[@class=\"btn btn-default button button-medium\"]")).click(); // в попапе жмем на кнопку Proceed to checkout и открывается корзина
        driver.findElement(By.xpath("//*[@id=\"layer_cart\"]/div[1]/div[2]/div[4]/a/span")).click(); // в попапе жмем на кнопку Proceed to checkout и открывается корзина

        Assert.assertEquals(firstProductFullName, driver.findElement(By.xpath("//*[@class=\"cart_description\"]//*[@class=\"product-name\"]")).getText()); //сравниваем имя продукта
        Assert.assertEquals(firstProductPrice, driver.findElement(By.xpath("//*[@class=\"cart_total\"]")).getText()); //сравниваем цену продукта
    }

    @After
    public void close() {
        driver.quit();
    }
}
