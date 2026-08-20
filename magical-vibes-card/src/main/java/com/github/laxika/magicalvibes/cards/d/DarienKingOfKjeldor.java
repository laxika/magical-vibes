package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "CSP", collectorNumber = "4")
public class DarienKingOfKjeldor extends Card {

    public DarienKingOfKjeldor() {
        // Whenever you're dealt damage, you may create that many 1/1 white Soldier creature tokens.
        addEffect(EffectSlot.ON_CONTROLLER_DEALT_DAMAGE,
                new MayEffect(
                        CreateTokenEffect.whiteSoldier(new EventValue()),
                        "Create that many 1/1 white Soldier creature tokens?"));
    }
}
