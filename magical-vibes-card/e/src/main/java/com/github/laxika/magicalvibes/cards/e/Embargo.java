package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingPermanentsDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "MMQ", collectorNumber = "77")
public class Embargo extends Card {

    public Embargo() {
        addEffect(EffectSlot.STATIC, new MatchingPermanentsDoesntUntapEffect(
                new PermanentNotPredicate(new PermanentIsLandPredicate())));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new LoseLifeEffect(2));
    }
}
