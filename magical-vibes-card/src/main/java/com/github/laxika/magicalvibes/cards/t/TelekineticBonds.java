package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;

@CardRegistration(set = "JUD", collectorNumber = "52")
public class TelekineticBonds extends Card {

    public TelekineticBonds() {
        MayPayManaEffect trigger = new MayPayManaEffect(
                "{1}{U}",
                new TapOrUntapTargetPermanentEffect(),
                "Pay {1}{U} to tap or untap target permanent?"
        );
        addEffect(EffectSlot.ON_OPPONENT_DISCARDS, trigger);
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS, trigger);
    }
}
