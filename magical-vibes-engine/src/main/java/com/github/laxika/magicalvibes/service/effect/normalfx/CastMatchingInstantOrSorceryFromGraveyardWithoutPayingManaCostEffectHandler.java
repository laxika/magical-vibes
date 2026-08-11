package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastMatchingInstantOrSorceryFromGraveyardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Queues independent resolution-time offers for all matching cards already in the controller's
 * graveyard. Each offer reuses the normal free graveyard-cast workflow.
 */
@Component
@RequiredArgsConstructor
public class CastMatchingInstantOrSorceryFromGraveyardWithoutPayingManaCostEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastMatchingInstantOrSorceryFromGraveyardWithoutPayingManaCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CastMatchingInstantOrSorceryFromGraveyardWithoutPayingManaCostEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null) {
            return;
        }

        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        CastTargetInstantOrSorceryFromGraveyardEffect castEffect =
                new CastTargetInstantOrSorceryFromGraveyardEffect(
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD, true);
        for (Card card : graveyard) {
            if (predicateEvaluationService.matchesCardPredicate(card, e.filter(), sourceCardId)) {
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        card, controllerId, List.of(castEffect),
                        entry.getCard().getName() + " — Cast " + card.getName()
                                + " without paying its mana cost?"));
            }
        }
    }
}
