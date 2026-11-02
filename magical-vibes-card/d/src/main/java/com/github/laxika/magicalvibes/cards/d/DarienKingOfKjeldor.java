package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CSP", collectorNumber = "4")
@CardRegistration(set = "A25", collectorNumber = "9")
@CardRegistration(set = "MKC", collectorNumber = "59")
public class DarienKingOfKjeldor extends Card {

    public DarienKingOfKjeldor() {
        // Whenever you're dealt damage, you may create that many 1/1 white Soldier creature tokens.
        addTrigger(Triggers.youAreDealtDamage(),
                new MayEffect(
                        CreateTokenEffect.whiteSoldier(new EventValue()),
                        "Create that many 1/1 white Soldier creature tokens?"));
    }
}
