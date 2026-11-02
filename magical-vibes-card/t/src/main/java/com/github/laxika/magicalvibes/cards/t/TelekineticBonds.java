package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JUD", collectorNumber = "52")
public class TelekineticBonds extends Card {

    public TelekineticBonds() {
        MayPayManaEffect trigger = new MayPayManaEffect(
                "{1}{U}",
                new MayEffect(new TapOrUntapTargetPermanentEffect(), "Tap or untap target permanent?"),
                "Pay {1}{U} to tap or untap target permanent?"
        );
        addTrigger(Triggers.opponentDiscardsCard(), trigger);
        addTrigger(Triggers.youDiscardCard(), trigger);
    }
}
