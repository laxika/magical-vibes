package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TDM", collectorNumber = "114")
public class NarsetsRebuke extends Card {

    public NarsetsRebuke() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new MarkTargetCreatureExileInsteadOfDieThisTurnEffect())
                .addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(5))
                .addEffect(EffectSlot.SPELL, new AwardManaEffect(ManaColor.BLUE))
                .addEffect(EffectSlot.SPELL, new AwardManaEffect(ManaColor.RED))
                .addEffect(EffectSlot.SPELL, new AwardManaEffect(ManaColor.WHITE));
    }
}
