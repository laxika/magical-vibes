package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "HOB", collectorNumber = "137")
public class ThroughTheForestGate extends Card {

    public ThroughTheForestGate() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsEffect(
                new Fixed(20), new Fixed(20), new CardTypePredicate(CardType.LAND),
                LookDestination.SHUFFLE_INTO_LIBRARY, false,
                LibrarySearchDestination.BATTLEFIELD_TAPPED, true));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(8));
    }
}
