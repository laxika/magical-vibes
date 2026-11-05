package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentManaEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "236")
@CardRegistration(set = "NEC", collectorNumber = "148")
public class ColossalPlow extends Card {

    public ColossalPlow() {
        addTrigger(Triggers.attacks(),
                new AwardPersistentManaEffect(ManaColor.WHITE, new Fixed(3)),
                new GainLifeEffect(3));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(6), AnimatePermanentsEffect.crew()),
                "Crew 6"
        ));
    }
}
