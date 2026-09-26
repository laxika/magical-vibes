package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "32")
@CardRegistration(set = "HOC", collectorNumber = "72")
public class GaladrielLightOfValinor extends Card {

    public GaladrielLightOfValinor() {
        // Whenever another creature you control enters, choose one that hasn't been chosen this
        // turn. The turn-scoped modal tracks each selected mode on Galadriel's permanent.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new ChooseModeNotYetChosenThisTurnEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Add {G}{G}{G}.",
                                new AwardManaEffect(ManaColor.GREEN, 3)),
                        new ChooseOneEffect.ChooseOneOption(
                                "Put a +1/+1 counter on each creature you control.",
                                new PutCounterOnEachControlledPermanentEffect(
                                        CounterType.PLUS_ONE_PLUS_ONE, 1,
                                        new PermanentIsCreaturePredicate())),
                        new ChooseOneEffect.ChooseOneOption(
                                "Scry 2, then draw a card.",
                                SequenceEffect.of(new ScryEffect(2), new DrawCardEffect(1)))
                )));
    }
}
