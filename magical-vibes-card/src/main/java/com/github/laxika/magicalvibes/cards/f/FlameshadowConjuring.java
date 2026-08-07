package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "ORI", collectorNumber = "147")
public class FlameshadowConjuring extends Card {

    public FlameshadowConjuring() {
        // Whenever a nontoken creature you control enters, you may pay {R}. If you do, create a token
        // that's a copy of that creature. That token gains haste. Exile it at the beginning of the next
        // end step.
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD, new MayPayManaEffect(
                "{R}",
                new CreateTokenCopyOfTargetPermanentEffect(true, true),
                "Pay {R} to create a hasty token copy of that creature (exiled at the beginning of the next end step)?"
        ));
    }
}
