package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateMayCastWithoutPayingManaEffect;
import com.github.laxika.magicalvibes.model.filter.CardManaValueAtMostSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "WHO", collectorNumber = "94")
@CardRegistration(set = "WHO", collectorNumber = "981")
@CardRegistration(set = "WHO", collectorNumber = "390")
@CardRegistration(set = "WHO", collectorNumber = "699")
public class RyanSinclair extends Card {

    public RyanSinclair() {
        addEffect(EffectSlot.ON_ATTACK, new RevealUntilCardPredicateMayCastWithoutPayingManaEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                false,
                new CardManaValueAtMostSourcePowerPredicate()));
    }
}
