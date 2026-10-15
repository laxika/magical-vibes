package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCountMatchingCardsToBattlefieldRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "ECL", collectorNumber = "165")
@CardRegistration(set = "ECL", collectorNumber = "323")
public class AuroraAwakener extends Card {

    public AuroraAwakener() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RevealUntilCountMatchingCardsToBattlefieldRestOnBottomRandomEffect(
                        new ColorsAmongControlledPermanents(), new CardIsPermanentPredicate()));
    }
}
