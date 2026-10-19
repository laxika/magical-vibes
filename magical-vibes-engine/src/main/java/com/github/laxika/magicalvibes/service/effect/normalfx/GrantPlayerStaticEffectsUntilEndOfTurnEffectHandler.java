package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEscapeToGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantPlayerStaticEffectsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardIdSetPredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrantPlayerStaticEffectsUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantPlayerStaticEffectsUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantPlayerStaticEffectsUntilEndOfTurnEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<CardEffect> granted = e.effects().stream()
                .map(grant -> grant instanceof GrantEscapeToGraveyardCardsEffect escape
                        ? snapshotEscapeGrant(gameData, controllerId, entry, escape)
                        : grant)
                .toList();
        gameData.playerStaticEffectsUntilEndOfTurn
                .computeIfAbsent(controllerId, ignored -> new java.util.ArrayList<>())
                .addAll(granted);
    }

    /**
     * Fixes the set of graveyard cards an escape grant applies to when it begins (CR 611.2c), so a
     * creature card that reaches the graveyard later in the turn does not gain escape.
     */
    private GrantEscapeToGraveyardCardsEffect snapshotEscapeGrant(GameData gameData, UUID controllerId,
                                                                  StackEntry entry,
                                                                  GrantEscapeToGraveyardCardsEffect escape) {
        Map<UUID, Long> versions = new HashMap<>();
        for (Card card : gameData.playerGraveyards.getOrDefault(controllerId, List.of())) {
            if (predicateEvaluationService.matchesCardPredicate(card, escape.filter(), entry.getCard().getId())) {
                versions.put(card.getId(), gameData.graveyardEntryVersion(card.getId()));
            }
        }
        return new GrantEscapeToGraveyardCardsEffect(
                new CardIdSetPredicate(versions.keySet(), versions),
                escape.alternateManaCost(),
                escape.additionalGraveyardExileCount());
    }
}
