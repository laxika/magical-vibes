package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SearchZonesForCardNamedToBattlefieldEffect;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "64")
public class ShaunRebeccaAgents extends Card {

    public ShaunRebeccaAgents() {
        // When Shaun & Rebecca enters, search your graveyard, hand, and library for The Animus
        // and put it onto the battlefield, then shuffle if you searched your library.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SearchZonesForCardNamedToBattlefieldEffect("The Animus"));

        // {T}: Add {C}. When you do, mill two cards.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardManaEffect(ManaColor.COLORLESS),
                        new MillEffect(2, MillRecipient.CONTROLLER)),
                "{T}: Add {C}. When you do, mill two cards."
        ));
    }
}
