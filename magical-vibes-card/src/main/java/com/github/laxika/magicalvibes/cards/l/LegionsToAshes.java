package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndAllControlledTokensWithSameNameEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BRO", collectorNumber = "215")
public class LegionsToAshes extends Card {

    public LegionsToAshes() {
        target(TargetFilters.nonlandPermanentAnOpponentControls())
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentAndAllControlledTokensWithSameNameEffect());
    }
}
