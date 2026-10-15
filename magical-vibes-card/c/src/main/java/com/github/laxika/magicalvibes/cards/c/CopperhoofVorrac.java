package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "MRD", collectorNumber = "116")
public class CopperhoofVorrac extends Card {

    public CopperhoofVorrac() {
        PermanentCount untappedOpponentsPermanents = new PermanentCount(
                new PermanentNotPredicate(new PermanentIsTappedPredicate()), CountScope.OPPONENTS);
        addEffect(EffectSlot.STATIC,
                new BoostSelfEffect(untappedOpponentsPermanents, untappedOpponentsPermanents));
    }
}
