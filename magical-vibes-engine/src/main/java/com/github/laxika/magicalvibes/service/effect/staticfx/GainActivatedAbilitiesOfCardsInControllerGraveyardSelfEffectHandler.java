package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfCardsInControllerGraveyardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * "This has all activated abilities of [matching] cards in your graveyard" (Trazyn the Infinite, Thranduil the
 * Elvenking). A card in a graveyard that has lost all its abilities (Yixlid Jailer) has none to give, as for the
 * all-graveyards borrower.
 */
@Component
@RequiredArgsConstructor
public class GainActivatedAbilitiesOfCardsInControllerGraveyardSelfEffectHandler
        implements StaticEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainActivatedAbilitiesOfCardsInControllerGraveyardEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        UUID controllerId = context.sourceControllerId();
        if (controllerId == null) return;

        GameData gameData = context.gameData();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null || gameQueryService.graveyardCardsHaveLostAllAbilities(gameData)) return;

        GainActivatedAbilitiesOfCardsInControllerGraveyardEffect gainEffect =
                (GainActivatedAbilitiesOfCardsInControllerGraveyardEffect) effect;
        for (Card card : graveyard) {
            if (!predicateEvaluationService.matchesCardPredicate(
                    card, gainEffect.filter(), null, gameData, controllerId)) {
                continue;
            }
            for (var ability : ManaAbilities.withIntrinsic(card)) {
                accumulator.addActivatedAbility(ability);
            }
        }
    }
}
