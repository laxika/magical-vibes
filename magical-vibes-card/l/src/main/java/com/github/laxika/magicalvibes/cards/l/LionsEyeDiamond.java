package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "307")
@CardRegistration(set = "VMA", collectorNumber = "271")
public class LionsEyeDiamond extends Card {

    public LionsEyeDiamond() {
        // Discard your hand, Sacrifice Lion's Eye Diamond: Add three mana of any one color.
        // Activate only as an instant: it needs priority, so it can't be activated mid-cast or mid-payment.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new DiscardHandCost(), new SacrificeSelfCost(), new AwardAnyColorManaEffect(3)),
                "Discard your hand, Sacrifice Lion's Eye Diamond: Add three mana of any one color. Activate only as an instant.",
                ActivationTimingRestriction.INSTANT_SPEED
        ));
    }
}
