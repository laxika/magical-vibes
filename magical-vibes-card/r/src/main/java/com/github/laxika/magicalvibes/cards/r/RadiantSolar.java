package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "AFC", collectorNumber = "9")
public class RadiantSolar extends Card {

    public RadiantSolar() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new VentureIntoDungeonEffect());
        addTrigger(Triggers.enters(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new VentureIntoDungeonEffect());

        addActivatedAbility(AbilityZones.HAND, new ActivatedAbility(
                false,
                "{W}",
                List.of(new VentureIntoDungeonEffect(), new GainLifeEffect(3)),
                "{W}, Discard this card: Venture into the dungeon and you gain 3 life."
        ));
    }
}
