package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryAndPerpetuallyReduceSoughtCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSharesColorWithControlledPermanentPredicate;

@CardRegistration(set = "YECL", collectorNumber = "18")
public class CircadianStruggle extends Card {

    public CircadianStruggle() {
        ColorsAmongControlledPermanents colorsAmongControlledPermanents =
                new ColorsAmongControlledPermanents();
        addEffect(EffectSlot.SPELL,
                new SeekLibraryAndPerpetuallyReduceSoughtCardsEffect(
                        colorsAmongControlledPermanents,
                        new CardSharesColorWithControlledPermanentPredicate(),
                        colorsAmongControlledPermanents));
    }
}
