package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.ReflectAllyDamageToDamagedCreatureControllerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "65")
public class FlayedNim extends Card {

    public FlayedNim() {
        addTrigger(Triggers.creatureYouControlDealsDamageToCreature(),
                ReflectAllyDamageToDamagedCreatureControllerEffect.selfCombatDamageCausesLifeLoss());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new RegenerateEffect()),
                "{2}{B}: Regenerate Flayed Nim."
        ));
    }
}
