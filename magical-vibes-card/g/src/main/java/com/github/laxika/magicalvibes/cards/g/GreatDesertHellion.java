package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourceIntensity;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawEffect;
import com.github.laxika.magicalvibes.model.effect.IntensifySourceCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YBRO", collectorNumber = "19")
public class GreatDesertHellion extends Card {

    public GreatDesertHellion() {
        setStartingIntensity(1);

        // At the beginning of your upkeep, sacrifice this creature unless you discard a card.
        // If you discard a card this way, this creature intensifies by 1.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new SacrificeUnlessDiscardCardTypeEffect(null, new IntensifySourceCardEffect(1)));

        // When this creature leaves the battlefield, you may discard your hand. If you do, draw
        // cards equal to its intensity.
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new MayEffect(
                new DiscardOwnHandThenDrawEffect(new SourceIntensity()),
                "Discard your hand and draw cards equal to Great Desert Hellion's intensity?"));
    }
}
