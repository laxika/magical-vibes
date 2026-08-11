package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "155")
public class CommuneWithTheGods extends Card {

    public CommuneWithTheGods() {
        CardAnyOfPredicate creatureOrEnchantment = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardTypePredicate(CardType.ENCHANTMENT)));
        addEffect(EffectSlot.SPELL,
                LookAtTopCardsEffect.mayRevealOneToHandRestToGraveyard(5, creatureOrEnchantment));
    }
}
