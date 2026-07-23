package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "ICE", collectorNumber = "89")
public class PolarKraken extends Card {

    public PolarKraken() {
        // Trample is auto-loaded from Scryfall.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        // Cumulative upkeep—Sacrifice a land.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                CumulativeUpkeepEffect.sacrifice(new PermanentIsLandPredicate()));
    }
}
