package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowPlayExiledCostCardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTargetCreatureCardIntoGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

import java.util.List;

@CardRegistration(set = "YLCI", collectorNumber = "9")
public class ChitinousCrawler extends Card {

    public ChitinousCrawler() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new ConjureDuplicateOfTargetCreatureCardIntoGraveyardEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new ExileNCardsFromGraveyardCost(
                                1, null, new CardIsPermanentPredicate(), false, true),
                        new AllowPlayExiledCostCardThisTurnEffect(true)),
                "Exile a permanent card from your graveyard: You may play it. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED)
                .withRequiredGraveyardCards(
                        new CardIsPermanentPredicate(), 8, "permanent cards in your graveyard"));
    }
}
