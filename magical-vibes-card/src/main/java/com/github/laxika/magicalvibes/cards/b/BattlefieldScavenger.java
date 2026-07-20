package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "AKH", collectorNumber = "118")
public class BattlefieldScavenger extends Card {

    public BattlefieldScavenger() {
        // Exert: "You may exert this creature as it attacks." (No self-bonus — plain Exert.) Modeled as
        // an optional attack trigger (matching Glory-Bound Initiate); choosing to exert keeps the
        // creature tapped through its next untap step.
        //
        // "Whenever you exert a creature, you may discard a card. If you do, draw a card." The engine has
        // no exert-event slot, so the only exert it can observe is this creature's own exert as it
        // attacks — the loot is nested as a second "may" that fires when the exert is accepted.
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                SequenceEffect.of(
                        new SkipNextUntapEffect(TapUntapScope.SELF),
                        new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?")
                ),
                "Exert Battlefield Scavenger as it attacks?"
        ));
    }
}
