package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "KLD", collectorNumber = "44")
public class DramaticReversal extends Card {

    public DramaticReversal() {
        addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(
                TapUntapScope.CONTROLLED,
                new PermanentNotPredicate(new PermanentIsLandPredicate())));
    }
}
