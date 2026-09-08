package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "126")
public class BlizzardSpecter extends Card {

    public BlizzardSpecter() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("That player returns a permanent they control to its owner's hand.",
                        new ReturnPermanentControlledByPlayerToHandEffect(new PermanentTruePredicate(), "permanent")),
                new ChooseOneEffect.ChooseOneOption("That player discards a card.",
                        new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER))
        )));
    }
}
