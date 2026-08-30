package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ResolveEffectOnTargetDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureCardFromTargetOwnerGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LEG", collectorNumber = "201")
public class Reincarnation extends Card {

    public Reincarnation() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new ResolveEffectOnTargetDeathThisTurnEffect(
                        new ReturnCreatureCardFromTargetOwnerGraveyardEffect()));
    }
}
