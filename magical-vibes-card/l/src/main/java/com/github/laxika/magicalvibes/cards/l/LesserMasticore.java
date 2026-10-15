package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "225")
public class LesserMasticore extends Card {

    public LesserMasticore() {
        addEffect(EffectSlot.SPELL, new DiscardCardTypeCost(null, null));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(new DealDamageToTargetCreatureEffect(1)),
                "This creature deals 1 damage to target creature.",
                TargetFilters.creature()
        ));
    }
}
