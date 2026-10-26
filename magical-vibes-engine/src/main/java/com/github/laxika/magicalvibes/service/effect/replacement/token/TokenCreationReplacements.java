package com.github.laxika.magicalvibes.service.effect.replacement.token;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.AddTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.model.replacement.TokenCreationEvent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.MaroGoneNutsSupport;
import com.github.laxika.magicalvibes.service.replacement.ReplacementCandidateSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Token-count replacements that apply to a token creation event, one option per effect instance:
 * Xorn's additional token, Doubling Season's and Parallel Lives' multiplier and Halving Season's
 * halving. They only rewrite counts, so they are pure; the player creating the tokens is
 * prompted only when the order in which they apply changes the tokens created (CR 616.1).
 *
 * <p>An effect rewrites the count of every group of tokens in the event it applies to ("twice that
 * many of those tokens"), including tokens an addition such as Queen Allenal of Ruadach put in the
 * event before it. An effect that adds tokens of a kind (Xorn) adds them once, to the first group
 * of that kind.
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
        Map<String, GameQueryService.TokenCreationReplacement> byKey = new LinkedHashMap<>();
        replacementsForBase(gameData, event).forEach(replacement -> byKey.putIfAbsent(replacement.key(), replacement));
        for (TokenCreationEvent.TokenGroup group : event.addedTokens()) {
            replacementsFor(gameData, event, group).forEach(replacement -> byKey.putIfAbsent(replacement.key(), replacement));
        }
        return byKey.values().stream()
                .map(replacement -> new ReplacementOption(replacement.key(), replacement.label(),
                        ReplacementOption.Kind.REPLACEMENT, true))
                .toList();
    }

    @Override
    public TokenCreationEvent transform(GameData gameData, TokenCreationEvent event, ReplacementOption option) {
        boolean addsOnce = false;
        boolean added = false;
        TokenCreationEvent result = event;
        Optional<GameQueryService.TokenCreationReplacement> base = find(replacementsForBase(gameData, event), option);
        if (base.isPresent()) {
            addsOnce = base.get().effect() instanceof AddTokenCreationEffect;
            result = result.withTokenCount(replace(gameData, base.get(), event.tokenCount()));
            added = true;
        }
        List<TokenCreationEvent.TokenGroup> groups = new ArrayList<>(event.addedTokens().size());
        for (TokenCreationEvent.TokenGroup group : event.addedTokens()) {
            Optional<GameQueryService.TokenCreationReplacement> forGroup =
                    added && addsOnce ? Optional.empty() : find(replacementsFor(gameData, event, group), option);
            if (forGroup.isPresent()) {
                addsOnce = forGroup.get().effect() instanceof AddTokenCreationEffect;
                added = true;
                groups.add(group.withCount(replace(gameData, forGroup.get(), group.count())));
            } else {
                groups.add(group);
            }
        }
        if (!added) {
            throw new IllegalStateException("Token creation replacement source left: " + option.key());
        }
        return result.withAddedTokens(groups);
    }

    private static int replace(GameData gameData, GameQueryService.TokenCreationReplacement replacement, int count) {
        return MaroGoneNutsSupport.apply(gameData, (CardEffect) replacement.effect(),
                replacement.effect().replaceTokenCount(count));
    }

    private static Optional<GameQueryService.TokenCreationReplacement> find(
            List<GameQueryService.TokenCreationReplacement> replacements, ReplacementOption option) {
        return replacements.stream().filter(candidate -> candidate.key().equals(option.key())).findFirst();
    }

    private List<GameQueryService.TokenCreationReplacement> replacementsForBase(GameData gameData,
                                                                               TokenCreationEvent event) {
        if (event.tokenCount() <= 0) {
            return List.of();
        }
        return gameQueryService.tokenCreationReplacements(gameData, event.controllerId(),
                event.token().subtypes(), event.creatureToken(), replacement -> true);
    }

    private List<GameQueryService.TokenCreationReplacement> replacementsFor(GameData gameData, TokenCreationEvent event,
                                                                           TokenCreationEvent.TokenGroup group) {
        return gameQueryService.tokenCreationReplacements(gameData, event.controllerId(),
                group.blueprint().subtypes(), group.creature(), replacement -> true);
    }
}
