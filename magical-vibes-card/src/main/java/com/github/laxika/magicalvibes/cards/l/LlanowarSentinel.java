package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaAndSearchLibraryForCardNamedToBattlefieldEffect;

@CardRegistration(set = "10E", collectorNumber = "275")
public class LlanowarSentinel extends Card {

    public LlanowarSentinel() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new PayManaAndSearchLibraryForCardNamedToBattlefieldEffect("{1}{G}", "Llanowar Sentinel"),
                "Pay {1}{G} to search your library for a card named Llanowar Sentinel and put it onto the battlefield?"
        ));
    }
}
