package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "119")
public class HenrikaDomnathi extends Card {

    public HenrikaDomnathi() {
        setBackFaceCard(new HenrikaInfernalSeer());

        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ChooseModeNotYetChosenEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Each player sacrifices a creature of their choice",
                        new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),
                                SacrificeRecipient.EACH_PLAYER)),
                new ChooseOneEffect.ChooseOneOption(
                        "You draw a card and you lose 1 life",
                        List.of(new DrawCardEffect(1), new LoseLifeEffect(1))),
                new ChooseOneEffect.ChooseOneOption(
                        "Transform Henrika",
                        new TransformSelfEffect()))));
    }

    @Override
    public String getBackFaceClassName() {
        return "HenrikaInfernalSeer";
    }
}
