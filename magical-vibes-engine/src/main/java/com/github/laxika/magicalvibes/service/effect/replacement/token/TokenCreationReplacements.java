package com.github.laxika.magicalvibes.service.effect.replacement.token;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.model.replacement.TokenCreationEvent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.MaroGoneNutsSupport;
import com.github.laxika.magicalvibes.service.replacement.ReplacementCandidateSource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Token-count replacements that apply to a token creation event, one option per effect instance:
 * Xorn's additional token, Doubling Season's and Parallel Lives' multiplier and Halving Season's
 * halving. They only rewrite the count, so they are pure; the player creating the tokens is
 * prompted only when the order in which they apply changes the number of tokens (CR 616.1).
 */
@Component
public class TokenCreationReplacements implements ReplacementCandidateSource<TokenCreationEvent> {

    private final GameQueryService gameQueryService;

    public TokenCreationReplacements(GameQueryService gameQueryService) {
        this.gameQueryService = gameQueryService;
    }

    @Override
    public Class<TokenCreationEvent> eventType() {
        return TokenCreationEvent.class;
    }

    @Override
    public List<ReplacementOption> applicable(GameData gameData, TokenCreationEvent event) {
        return replacements(gameData, event).stream()
                .map(replacement -> new ReplacementOption(replacement.key(), replacement.label(),
                        ReplacementOption.Kind.REPLACEMENT, true))
                .toList();
    }

    @Override
    public TokenCreationEvent transform(GameData gameData, TokenCreationEvent event, ReplacementOption option) {
        GameQueryService.TokenCreationReplacement replacement = replacements(gameData, event).stream()
                .filter(candidate -> candidate.key().equals(option.key()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Token creation replacement source left: " + option.key()));
        return event.withAmount(MaroGoneNutsSupport.apply(gameData, (CardEffect) replacement.effect(),
                replacement.effect().replaceTokenCount(event.amount())));
    }

    private List<GameQueryService.TokenCreationReplacement> replacements(GameData gameData, TokenCreationEvent event) {
        if (event.amount() <= 0) {
            return List.of();
        }
        return gameQueryService.tokenCreationReplacements(gameData, event.controllerId(),
                event.token().subtypes(), event.creatureToken(), replacement -> true);
    }
}
