package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceAttackedOrBlockedThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfIntoOwnerLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M19", collectorNumber = "148")
public class InfernoHellion extends Card {

    public InfernoHellion() {
        // Trample is auto-loaded from Scryfall.
        // At the beginning of each end step, if this creature attacked or blocked this turn,
        // its owner shuffles it into their library.
        addTrigger(Triggers.endStep(ActivePlayer.ANY), new ConditionalEffect(
                new SourceAttackedOrBlockedThisTurn(),
                new ShuffleSelfIntoOwnerLibraryEffect()));
    }
}
