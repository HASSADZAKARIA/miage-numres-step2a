package org.aepsilon.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.aepsilon.dto.CategoryDto;
import org.aepsilon.dto.ProposalDto;
import org.aepsilon.dto.QuestionDto;
import org.aepsilon.dto.TranslationDto;
import org.aepsilon.orm.Category;
import org.aepsilon.orm.Proposal;
import org.aepsilon.orm.Question;
import org.aepsilon.web.client.TranslateClient;
import org.aepsilon.web.client.TranslateRequest;
import org.aepsilon.web.client.TranslateResponse;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class TranslateService {

    @ConfigProperty(name = "api-quizz.default-language", defaultValue = "fr")
    private String defaultLanguage;
    @ConfigProperty(name = "api-quizz.translations", defaultValue = "en")
    private String translatedLanguage;

    @Inject
    @RestClient
    TranslateClient client;

    public List<QuestionDto> translateQuestions(List<Question> questions) {
        List<QuestionDto> result = new ArrayList<>();
        for (Question currentQuestion : questions) {
            result.add(translateOneQuestion(currentQuestion));
        } // End For Each
        return result;
    }

    public QuestionDto translateOneQuestion(Question currentQuestion) {
        
        QuestionDto q = new QuestionDto(currentQuestion, defaultLanguage);

        
        q.translations = translateLabel(currentQuestion.label);

        
        q.catgory = translateOneCategory(currentQuestion.category);

        return q;
    }

    public CategoryDto translateOneCategory(Category currentCategory) {
        
        CategoryDto c = new CategoryDto(currentCategory, defaultLanguage);

       
        c.translations = translateLabel(currentCategory.label);

        return c;
    }

    public List<ProposalDto> translateProposals(List<Proposal> proposals) {
        
        return proposals.stream()
                .map(this::translateOneProposal)
                .collect(Collectors.toList());
    }

    public ProposalDto translateOneProposal(Proposal currentProposal) {
        
        ProposalDto p = new ProposalDto(currentProposal, defaultLanguage);

        // Traduire le label de la proposition
        p.translations = translateLabel(currentProposal.label);

        // Traduire la question associée
        p.question = translateOneQuestion(currentProposal.question);

        return p;
    }

    private List<TranslationDto> translateLabel(String label) {
        List<TranslationDto> translations = new ArrayList<>();
        String[] languages = translatedLanguage.split(",");

        for (String currentLanguage : languages) {
            TranslateRequest r = new TranslateRequest();
            r.setSource(defaultLanguage);
            r.setTarget(currentLanguage);
            r.setQ(label);
            r.setAlternatives(0);
            r.setFormat("text");

            TranslateResponse rep = client.translate(r);
            translations.add(new TranslationDto(rep, currentLanguage));
        }

        return translations;
    }

}
