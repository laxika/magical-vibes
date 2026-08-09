package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.ReflectAllyDamageToDamagedCreatureControllerEffect;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "65")
public class FlayedNim extends Card {

    public FlayedNim() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE,
                ReflectAllyDamageToDamagedCreatureControllerEffect.selfCombatDamageCausesLifeLoss());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new RegenerateEffect()),
                "{2}{B}: Regenerate Flayed Nim."
        ));
    }
}
