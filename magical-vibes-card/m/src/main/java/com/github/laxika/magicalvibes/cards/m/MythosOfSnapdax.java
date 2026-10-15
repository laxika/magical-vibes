package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "24")
public class MythosOfSnapdax extends Card {

    public MythosOfSnapdax() {
        ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect eachPlayerChooses =
                new ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect(
                        List.of(CardType.ARTIFACT, CardType.CREATURE, CardType.ENCHANTMENT, CardType.PLANESWALKER),
                        false, true);
        ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect controllerChooses =
                new ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect(
                        List.of(CardType.ARTIFACT, CardType.CREATURE, CardType.ENCHANTMENT, CardType.PLANESWALKER),
                        false, false);

        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new AllConditions(List.of(
                        new ColorSpentToCast(ManaColor.BLACK),
                        new ColorSpentToCast(ManaColor.RED))),
                eachPlayerChooses, controllerChooses));
    }
}
