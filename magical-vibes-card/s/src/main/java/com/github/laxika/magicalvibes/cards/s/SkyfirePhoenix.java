package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntrySourceIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "C19", collectorNumber = "28")
public class SkyfirePhoenix extends Card {

    public SkyfirePhoenix() {
        addTrigger(new TriggerCondition(Triggers.youCastSpell(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT, new SpellCastTriggerEffect(
                null,
                List.of(new ReturnSourceCardFromGraveyardToBattlefieldEffect(false)),
                new StackEntrySourceIsCommanderPredicate()
        ));
    }
}
