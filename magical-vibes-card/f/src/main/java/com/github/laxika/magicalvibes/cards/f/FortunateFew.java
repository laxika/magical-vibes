package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesDifferentOpponentPermanentThenDestroyRestEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "C17", collectorNumber = "4")
public class FortunateFew extends Card {

    public FortunateFew() {
        addEffect(EffectSlot.SPELL,
                new EachPlayerChoosesDifferentOpponentPermanentThenDestroyRestEffect(
                        new PermanentNotPredicate(new PermanentIsLandPredicate())));
    }
}
