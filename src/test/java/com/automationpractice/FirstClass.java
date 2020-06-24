package com.automationpractice;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(JUnitParamsRunner.class)
public class FirstClass {

    public ChromeDriver driver;

    @Before
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:\\Webdrivers\\chromedriver.exe");
        driver = new ChromeDriver();
    }

    @Test
    @Parameters({"Summer", "Dress", "t-shirt"})
    public void searchTest(String searchText) throws InterruptedException, IOException {

        // 1. открываем сайт http://automationpractice.com/
        driver.get("http://automationpractice.com/index.php");

        // 2. в поле поиска вводим ключевое слово: 'Summer' и нажимаем значок поиска (лупу)
        WebElement element = driver.findElement(By.xpath("//*[@id='search_query_top']"));
        element.click();
        element.sendKeys(searchText);
        driver.findElement(By.xpath("//*[@name='submit_search']")).click();
        WebElement dynamicElement = (new WebDriverWait(driver, 10))
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='lighter']")));

        // 3. проверяем, что над списком продуктов в надписи 'SEARCH' отображается наш поисковый запрос - "SUMMER"
        String expectedSearchText = driver.findElement(By.xpath("//*[@class='lighter']")).getText().substring(1);
        expectedSearchText = expectedSearchText.substring(0, expectedSearchText.length() - 1);
        Assert.assertEquals(searchText.toUpperCase(), expectedSearchText);

        // 4. открываем дропдаун сортировки и выбираем опцию 'Price: Highest first'
        WebElement dynamicDropDown = (new WebDriverWait(driver, 20))
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='selectProductSort']")));
        driver.findElement(By.xpath("//*[@id='selectProductSort']")).click();
        driver.findElement(By.xpath("//*[@id='selectProductSort']/option[contains(text(),'Price: Highest first')]")).click();

        // 5. проверяем, что элементы отсортированы в соответствии с выбранной опцией (сейчас сортировка идёт по старой цене - если у товара есть скидка, нужно смотреть на старую цену)
        // Создаем коллекцию вебэлементов продуктовых контейнеров
        List<WebElement> productContainerList = driver.findElements(By.xpath("//*[@class='product-container']"));
        // Создаем массив цен в float (если есть старая цена, то берем ее. Если старой нет - берем новую)
        List<Float> actualProductPriceList = new ArrayList<>();
        for (WebElement webElement : productContainerList) {
            try {
                actualProductPriceList.add(Float.parseFloat(webElement.findElement(By.xpath(".//*[@class='right-block']//*[@class='old-price product-price']")).getText().substring(1)));
            } catch (NoSuchElementException e) {
                actualProductPriceList.add(Float.parseFloat(webElement.findElement(By.xpath(".//*[@class='right-block']//*[@class='price product-price']")).getText().substring(1)));
            }
        }
        // Создаем копию массива цен
        List<Float> expectedProductPriceList = new ArrayList<>();
        expectedProductPriceList.addAll(actualProductPriceList);
        // Сортируем по убыванию копию массива цен
        Collections.sort(expectedProductPriceList, Collections.reverseOrder());
        // Сравним два списка
        Assert.assertEquals(expectedProductPriceList, actualProductPriceList);

        // 6. берем первый из найденных товаров и запоминаем его полное название и цену
        WebElement dynamicProductName = (new WebDriverWait(driver, 20))
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='center_column']//*[@class='product-name']")));
        String firstProductFullName = driver.findElement(By.xpath("//*[@id='center_column']//*[@class='product-name']")).getText();
        String firstProductPrice = driver.findElement(By.xpath("//*[@class='right-block']//*[@class='price product-price']")).getText();

        // 7. добавляем его в корзину
        driver.findElement(By.xpath("//*[contains(@class, 'button ajax_add_to_cart')]")).click();

        // 8. открываем корзину и сравниваем название и цену в колонке "Total" у товара, на соответствие с сохраненными значениями
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@class='btn btn-default button button-medium']")));
        driver.findElement(By.xpath("//*[@class='btn btn-default button button-medium']")).click(); // в попапе жмем на кнопку Proceed to checkout и открывается корзина
        Assert.assertEquals(firstProductFullName, driver.findElement(By.xpath("//*[@class='cart_description']//*[@class='product-name']")).getText()); //сравниваем имя продукта
        Assert.assertEquals(firstProductPrice, driver.findElement(By.xpath("//*[@class='cart_total']")).getText()); //сравниваем цену продукта
    }

    @After
    public void close() {
        driver.quit();
    }
}
