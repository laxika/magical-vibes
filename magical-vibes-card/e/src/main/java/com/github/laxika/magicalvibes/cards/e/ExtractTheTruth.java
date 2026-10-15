package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "78")
public class ExtractTheTruth extends Card {

    public ExtractTheTruth() {
        var opponentFilter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent.");

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent reveals their hand. You may choose a creature, enchantment, or planeswalker card from it. That player discards that card",
                        new ChooseCardsFromTargetHandEffect(
                                1,
                                List.of(CardType.CREATURE, CardType.ENCHANTMENT, CardType.PLANESWALKER),
                                HandChoiceDestination.DISCARD,
                                null),
                        opponentFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent sacrifices an enchantment of their choice",
                        new SacrificePermanentsEffect(
                                1, new PermanentIsEnchantmentPredicate(), SacrificeRecipient.TARGET_PLAYER),
                        opponentFilter)
        )));
    }
}
