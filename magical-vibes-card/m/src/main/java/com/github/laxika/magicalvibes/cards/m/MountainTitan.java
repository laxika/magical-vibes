package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "299")
public class MountainTitan extends Card {

    public MountainTitan() {
        // {1}{R}{R}: Until end of turn, whenever you cast a black spell, put a +1/+1 counter on this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}{R}",
                List.of(new RegisterDelayedControllerSpellCastTriggerEffect(
                        new CardColorPredicate(CardColor.BLACK),
                        List.of(new PutCountersOnSourceEffect(1, 1, 1)))),
                "{1}{R}{R}: Until end of turn, whenever you cast a black spell, put a +1/+1 counter on this creature."));
    }
}
