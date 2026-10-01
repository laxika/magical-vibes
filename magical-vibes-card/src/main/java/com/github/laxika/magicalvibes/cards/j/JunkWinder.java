package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MH2", collectorNumber = "48")
@CardRegistration(set = "MOC", collectorNumber = "225")
@CardRegistration(set = "MKC", collectorNumber = "107")
@CardRegistration(set = "BLC", collectorNumber = "169")
public class JunkWinder extends Card {

    public JunkWinder() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new PermanentCount(new PermanentIsTokenPredicate(), CountScope.CONTROLLER)));

        target(TargetFilters.nonlandPermanentAnOpponentControls()).addEffect(
                EffectSlot.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD,
                SequenceEffect.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new SkipNextUntapEffect(TapUntapScope.TARGET)));
    }
}
