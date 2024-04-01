package undertaken.lab1.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import undertaken.lab1.entity.Language;
import undertaken.lab1.entity.Text;
import undertaken.lab1.repository.LanguageRepository;
import undertaken.lab1.repository.TextRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

class CrudOperationTest {
    @Mock
    private TextRepository textRepository;

    @Mock
    private LanguageRepository languageRepository;

    @InjectMocks
    private CrudOperation crudOperation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void addTextAndDetectLanguage() {
        ServiceApiKey serviceApiKey = mock(ServiceApiKey.class);
        LanguageDetectiveService languageDetectiveService = mock(LanguageDetectiveService.class);
        NameLanguageService nameLanguageService = mock(NameLanguageService.class);
        TextLanguageService textLanguageService = mock(TextLanguageService.class);
        TextRepository textRepository = mock(TextRepository.class);
        LanguageRepository languageRepository = mock(LanguageRepository.class);

        String apiKey = "testApiKey";
        when(serviceApiKey.getApiKey()).thenReturn(apiKey);

        String detectedLanguage = "English";
        when(languageDetectiveService.detectLanguage(any(), eq(apiKey))).thenReturn(detectedLanguage);

        // Создание экземпляра класса, который тестируем
        CrudOperation crudOperation = new CrudOperation(textLanguageService, languageDetectiveService, nameLanguageService, serviceApiKey, textRepository, languageRepository);

        // Вызов тестируемого метода
        String text = "Test text";
        String result = crudOperation.addTextAndDetectLanguage(text);

        // Проверка ожидаемого результата
        String expectedResult = "Text and language saved successfully";
        assertEquals(expectedResult, result);

        // Проверка вызовов методов зависимостей
        verify(serviceApiKey, times(1)).getApiKey();
        verify(languageDetectiveService, times(1)).detectLanguage(any(), eq(apiKey));
        verify(nameLanguageService, times(1)).findByName(detectedLanguage);
        verify(nameLanguageService, times(1)).save(detectedLanguage);
        verify(textLanguageService, times(1)).save(any());
    }

    @Test
    void deleteText_TextFoundAndDeletedSuccessfully()  {
        String textContent = "Hello";
        Text text = new Text();
        text.setText(textContent);
        when(textRepository.findByText(textContent)).thenReturn(text);
        String result = crudOperation.deleteText(textContent);
        assertEquals("Text deleted successfully", result);
        verify(textRepository, times(1)).delete(text);
    }

    @Test
    void deleteText_TextNotFound() {
        String textContent = "UnknownText";
        when(textRepository.findByText(textContent)).thenReturn(null);

        String result = crudOperation.deleteText(textContent);
        assertEquals("Text not found", result);
        verify(textRepository, never()).delete(any(Text.class));
    }
    @Test
    void deleteLanguageAndText_LanguageFoundAndDeletedSuccessfully() {
        String languageName = "English";
        Language language = new Language();
        language.setName(languageName);

        List<Text> texts = new ArrayList<>();
        Text text1 = new Text();
        text1.setText("Hello");
        texts.add(text1);

        Text text2 = new Text();
        text2.setText("World");
        texts.add(text2);
        language.setTextLanguages(texts);
        when(languageRepository.findByName(languageName)).thenReturn(language);

        String result = crudOperation.deleteLanguageAndText(languageName);

        assertEquals("Language and associated texts deleted successfully", result);
        verify(textRepository, times(1)).deleteAll(texts);
        verify(languageRepository, times(1)).delete(language);
    }

    @Test
    void deleteLanguageAndText_LanguageNotFound() {
        String languageName = "UnknownLanguage";
        when(languageRepository.findByName(languageName)).thenReturn(null);

        String result = crudOperation.deleteLanguageAndText(languageName);

        assertEquals("Language not found", result);
        verify(textRepository, never()).deleteAll(anyList());
        verify(languageRepository, never()).delete(any(Language.class));
    }

    @Test
    void updateText_TextFoundAndUpdatedSuccessfully() {
        Long textID = 1L;
        String newText = "Updated text";
        Text text = new Text();
        text.setId(textID);
        when(textRepository.findById(textID)).thenReturn(Optional.of(text));

        String result = crudOperation.updateText(textID, newText);

        assertEquals("Text updated successfully", result);
        assertEquals(newText, text.getText());
        verify(textRepository, times(1)).save(text);
    }

    @Test
    void updateText_TextNotFound() {
        Long textID = 1L;
        String newText = "Updated text";
        when(textRepository.findById(textID)).thenReturn((Optional.empty()));

        String result = crudOperation.updateText(textID, newText);

        assertEquals("Text not found", result);
        verify(textRepository, never()).save(any(Text.class));
    }
}