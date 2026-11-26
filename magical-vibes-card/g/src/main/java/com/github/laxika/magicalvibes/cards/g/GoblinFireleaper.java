package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "HOC", collectorNumber = "195")
public class GoblinFireleaper extends Card {

    public GoblinFireleaper() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{R}", List.of(BoostEffect.self(1, 0)),
                "{1}{R}: This creature gets +1/+0 until end of turn."));
        target(TargetFilters.creatureAnOpponentControls()).addTrigger(Triggers.dies(ObjectRelation.THIS),
                DealDamageEffect.toTargetCreature(new SourcePower()));
    }
}
