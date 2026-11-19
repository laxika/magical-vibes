package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "150")
public class HealingLeaves extends Card {

    public HealingLeaves() {
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target player gains 3 life",
                        new GainLifeEffect(3, Subjects.targetPlayer())),
                new ChooseOneEffect.ChooseOneOption(
                        "Prevent the next 3 damage that would be dealt to any target this turn",
                        PreventDamageEffect.nextToTarget(3))
        )));
    }
}
