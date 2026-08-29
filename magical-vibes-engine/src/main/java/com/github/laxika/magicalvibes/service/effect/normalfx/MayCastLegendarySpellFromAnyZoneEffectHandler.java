package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastLegendarySpellFromAnyZoneEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Queues one free-cast choice for each matching card in the three zones Kaya's emblem can use. */
@Component
@RequiredArgsConstructor
public class MayCastLegendarySpellFromAnyZoneEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastLegendarySpellFromAnyZoneEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MayCastLegendarySpellFromAnyZoneEffect castEffect =
                (MayCastLegendarySpellFromAnyZoneEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> eligible = new ArrayList<>();

        addMatching(eligible, gameData.playerHands.get(controllerId), castEffect);
        addMatching(eligible, gameData.playerGraveyards.get(controllerId), castEffect);
        for (ExiledCardEntry exiled : List.copyOf(gameData.exiledCards)) {
            if (controllerId.equals(exiled.ownerId())
                    && (!exiled.faceDown() || gameData.foretoldCardIds.contains(exiled.card().getId()))
                    && matches(exiled.card(), castEffect)) {
                eligible.add(exiled.card());
            }
        }

        for (int i = eligible.size() - 1; i >= 0; i--) {
            Card card = eligible.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    controllerId,
                    List.of(castEffect),
                    "Cast " + card.getName() + " without paying its mana cost?"
            ));
        }
    }

    private void addMatching(List<Card> eligible, List<Card> cards,
                             MayCastLegendarySpellFromAnyZoneEffect effect) {
        if (cards == null) {
            return;
        }
        for (Card card : cards) {
            if (matches(card, effect)) {
                eligible.add(card);
            }
        }
    }

    private boolean matches(Card card, MayCastLegendarySpellFromAnyZoneEffect effect) {
        return effect.filter() == null
                || predicateEvaluationService.matchesCardPredicate(card, effect.filter(), null);
    }
}
