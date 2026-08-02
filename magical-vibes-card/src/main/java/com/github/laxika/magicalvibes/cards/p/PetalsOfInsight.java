package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutTopCardsOfLibraryOnBottomEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;

@CardRegistration(set = "CHK", collectorNumber = "79")
public class PetalsOfInsight extends Card {

    public PetalsOfInsight() {
        // Look at the top three cards of your library. You may put those cards on the bottom of
        // your library in any order. If you do, return Petals of Insight to its owner's hand.
        // Otherwise, draw three cards.
        //
        // The peek comes first so the choice is informed (no target = own library). Accepting
        // returns the spell off the stack to hand and bottoms the three cards in a chosen order;
        // declining draws the same three cards.
        addEffect(EffectSlot.SPELL,
                new LookAtTopCardsOfTargetLibraryEffect(3, TargetLibraryAction.LOOK_ONLY));
        addEffect(EffectSlot.SPELL, new MayEffect(
                SequenceEffect.of(
                        ReturnToHandEffect.selfSpell(),
                        new PutTopCardsOfLibraryOnBottomEffect(3)),
                "put those cards on the bottom of your library in any order and return this spell to your hand?",
                new DrawCardEffect(3)));
    }
}
