package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ALA", collectorNumber = "108")
@CardRegistration(set = "AKH", collectorNumber = "141")
@CardRegistration(set = "JOU", collectorNumber = "103")
@CardRegistration(set = "AKR", collectorNumber = "164")
public class MagmaSpray extends Card {

    public MagmaSpray() {
        // Magma Spray deals 2 damage to target creature. If that creature would die this
        // turn, exile it instead.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new MarkTargetCreatureExileInsteadOfDieThisTurnEffect())
                .addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(2));
    }
}
