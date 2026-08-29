package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GiftsUngivenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "TLA", collectorNumber = "178")
public class ElementalTeachings extends Card {

    public ElementalTeachings() {
        addEffect(EffectSlot.SPELL, new GiftsUngivenEffect(
                new CardTypePredicate(CardType.LAND), false,
                CardPileDisposition.GIFTS_UNGIVEN_BATTLEFIELD_TAPPED));
    }
}
