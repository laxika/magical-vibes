package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "91")
@CardRegistration(set = "C17", collectorNumber = "130")
public class VeinDrinker extends Card {

    public VeinDrinker() {
        // {R}, {T}: This creature deals damage equal to its power to target creature.
        // That creature deals damage equal to its power to this creature.
        // The damage is one-sided when this creature has left the battlefield: it still deals damage
        // using its last known power, and the target deals nothing back.
        addActivatedAbility(new ActivatedAbility(
                true, "{R}",
                List.of(new SourceFightsTargetCreatureEffect(false)),
                "{R}, {T}: This creature deals damage equal to its power to target creature. "
                        + "That creature deals damage equal to its power to this creature.",
                TargetFilters.creature()
        ));

        // Whenever a creature dealt damage by this creature this turn dies, put a +1/+1 counter on this creature.
        addTrigger(Triggers.creatureDies(ObjectRelation.DAMAGED_BY_THIS), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
