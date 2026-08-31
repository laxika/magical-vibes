package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "168")
public class Whetwheel extends Card {

    public Whetwheel() {
        addMorph("{3}");
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{X}",
                List.of(new MillEffect(new XValue(), MillRecipient.TARGET_PLAYER)),
                "{X}{X}, {T}: Target player mills X cards."
        ));
    }
}
