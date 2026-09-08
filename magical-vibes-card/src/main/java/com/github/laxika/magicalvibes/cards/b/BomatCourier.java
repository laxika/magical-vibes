package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardHandCost;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "199")
public class BomatCourier extends Card {

    public BomatCourier() {
        addEffect(EffectSlot.ON_ATTACK, new ExileTopCardsToSourceEffect(1, true));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(
                        new DiscardHandCost(),
                        new SacrificeSelfCost(),
                        new PutAllCardsExiledWithSourceIntoOwnersHandsEffect()),
                "{R}, Discard your hand, Sacrifice this creature: Put all cards exiled with this creature into their owners' hands."
        ));
    }
}
