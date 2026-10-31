package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "17")
public class ExcavationElephant extends Card {

    public ExcavationElephant() {
        // Kicker {1}{W}
        addStaticEffect(new KickerEffect("{1}{W}"));

        // When this creature enters, if it was kicked, return target artifact card
        // from your graveyard to your hand.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new Kicked(),
                new ReturnTargetCardsFromGraveyardToHandEffect(
                        new CardTypePredicate(CardType.ARTIFACT), 1, null, false, true, 1,
                        false, Set.of(), false)
        ));
    }
}
