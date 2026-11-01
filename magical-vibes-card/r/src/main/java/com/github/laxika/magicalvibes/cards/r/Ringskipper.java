package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "81")
public class Ringskipper extends Card {

    public Ringskipper() {
        // Flying is auto-loaded from Scryfall.
        // When this creature dies, clash with an opponent. If you win, return this card to its owner's hand.
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new ClashEffect(new ReturnSourceCardFromGraveyardToOwnerHandEffect()));
    }
}
