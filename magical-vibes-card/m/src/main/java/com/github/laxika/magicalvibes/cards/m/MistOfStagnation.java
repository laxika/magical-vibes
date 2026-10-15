package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.MatchingPermanentsDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsForAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "JUD", collectorNumber = "48")
public class MistOfStagnation extends Card {

    public MistOfStagnation() {
        addEffect(EffectSlot.STATIC, new MatchingPermanentsDoesntUntapEffect(new PermanentTruePredicate()));
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new UntapPermanentsForAmountEffect(
                        new CardsInGraveyard(null, CountScope.TARGET_PLAYER),
                        new PermanentTruePredicate()));
    }
}
