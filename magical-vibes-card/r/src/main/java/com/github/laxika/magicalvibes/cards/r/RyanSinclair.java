package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateMayCastWithoutPayingManaEffect;
import com.github.laxika.magicalvibes.model.filter.CardManaValueAtMostSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "94")
@CardRegistration(set = "WHO", collectorNumber = "981")
@CardRegistration(set = "WHO", collectorNumber = "390")
@CardRegistration(set = "WHO", collectorNumber = "699")
public class RyanSinclair extends Card {

    public RyanSinclair() {
        addTrigger(Triggers.attacks(), RevealUntilCardPredicateMayCastWithoutPayingManaEffect.exilingUntil(
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                new CardManaValueAtMostSourcePowerPredicate()));
    }
}
