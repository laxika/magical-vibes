package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "113")
public class Misfortune extends Card {

    public Misfortune() {
        addSpellEffect(ChooseOneEffect.anOpponentChooses(List.of(
                new ChooseOneOption("You put a +1/+1 counter on each creature you control and gain 4 life",
                        List.of(PutCountersEffect.onEachYouControl(CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()),
                                new GainLifeEffect(4))),
                new ChooseOneOption("You put a -1/-1 counter on each creature that player controls and"
                        + " Misfortune deals 4 damage to that player",
                        List.of(new PutCountersEffect(
                                        Subjects.controlledBy(Subjects.modeChooser(), new PermanentIsCreaturePredicate()),
                                        CounterType.MINUS_ONE_MINUS_ONE),
                                DealDamageEffect.to(Subjects.modeChooser(), 4))))));
    }
}
