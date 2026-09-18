package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentAndReturnTargetCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "USG", collectorNumber = "166")
@CardRegistration(set = "EMA", collectorNumber = "113")
@CardRegistration(set = "SPG", collectorNumber = "23")
@CardRegistration(set = "C14", collectorNumber = "169")
public class Victimize extends Card {

    public Victimize() {
        // Choose two target creature cards in your graveyard. Sacrifice a creature. If you do,
        // return the chosen cards to the battlefield tapped.
        addEffect(EffectSlot.SPELL, new SacrificePermanentAndReturnTargetCardsFromGraveyardEffect(
                new PermanentIsCreaturePredicate(),
                new CardTypePredicate(CardType.CREATURE),
                2,
                true,
                "a creature"));
    }
}
