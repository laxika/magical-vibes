package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "216")
public class SandsOfDelirium extends Card {

    public SandsOfDelirium() {
        // {X}, {T}: Target player mills X cards.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(new MillEffect(new XValue(), MillRecipient.TARGET_PLAYER)),
                "{X}, {T}: Target player mills X cards."
        ));
    }
}
