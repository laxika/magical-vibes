package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDiscardedCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "222")
public class BagOfHolding extends Card {

    public BagOfHolding() {
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS, new ExileDiscardedCardFromGraveyardEffect(true));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new DrawCardEffect(1), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "{2}, {T}: Draw a card, then discard a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new SacrificeSelfCost(), new PutAllCardsExiledWithSourceIntoOwnersHandsEffect()),
                "{4}, {T}, Sacrifice this artifact: Return all cards exiled with this artifact to their owner's hand."
        ));
    }
}
