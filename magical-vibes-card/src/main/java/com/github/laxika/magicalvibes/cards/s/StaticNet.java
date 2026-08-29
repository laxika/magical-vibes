package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BRO", collectorNumber = "27")
public class StaticNet extends Card {

    public StaticNet() {
        target(TargetFilters.nonlandPermanentAnOpponentControls())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileTargetPermanentUntilSourceLeavesEffect())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(2))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofPowerstoneToken(new Fixed(1)));
    }
}
