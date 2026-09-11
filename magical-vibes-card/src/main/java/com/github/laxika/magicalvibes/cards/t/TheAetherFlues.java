package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCreatureToBattlefieldRestToLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "OHOP", collectorNumber = "2")
public class TheAetherFlues extends Card {

    public TheAetherFlues() {
        MayEffect sacrificeThenReveal = new MayEffect(
                new SacrificePermanentsThenEffect(
                        1,
                        new PermanentIsCreaturePredicate(),
                        new RevealUntilCreatureToBattlefieldRestToLibraryEffect(),
                        "a creature"),
                "Sacrifice a creature?");
        addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED, sacrificeThenReveal);
        addEffect(EffectSlot.UPKEEP_TRIGGERED, sacrificeThenReveal);
        addEffect(EffectSlot.CHAOS_TRIGGERED, new MayEffect(
                new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.CREATURE), "creature"),
                "Put a creature card from your hand onto the battlefield?"));
    }
}
