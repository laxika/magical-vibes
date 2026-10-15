package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TreasureManaSpentToCast;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AFR", collectorNumber = "205")
public class SpoilsOfTheHunt extends Card {

    public SpoilsOfTheHunt() {
        SpellTarget sourceTarget = target(TargetFilters.creatureYouControl());
        sourceTarget.addEffect(EffectSlot.SPELL,
                new BoostTargetCreatureEffect(new TreasureManaSpentToCast(), new Fixed(0)));

        SpellTarget victimTarget = target(TargetFilters.creatureAnOpponentControls());
        victimTarget.addEffect(EffectSlot.SPELL, new TargetCreatureDealsPowerDamageToAnyTargetEffect());
    }
}
