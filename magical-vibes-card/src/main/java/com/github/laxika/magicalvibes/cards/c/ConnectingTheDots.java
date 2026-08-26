package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardHandCost;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "118")
public class ConnectingTheDots extends Card {

    public ConnectingTheDots() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS, new ExileTopCardsToSourceEffect(1, true));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(
                        new DiscardHandCost(),
                        new SacrificeSelfCost(),
                        new PutAllCardsExiledWithSourceIntoOwnersHandsEffect()),
                "{1}{R}, Discard your hand, Sacrifice this enchantment: Put all cards exiled with this enchantment into their owners' hands."
        ));
    }
}
