package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "UDS", collectorNumber = "134")
public class Masticore extends Card {

    public Masticore() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificeUnlessDiscardCardTypeEffect(null));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new DealDamageToTargetCreatureEffect(1)),
                "This creature deals 1 damage to target creature.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new RegenerateEffect()),
                "Regenerate this creature."
        ));
    }
}
