package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsAndMayCastSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "HA4", collectorNumber = "19")
public class CollectedConjuring extends Card {

    public CollectedConjuring() {
        addEffect(EffectSlot.SPELL, new ExileTopCardsAndMayCastSpellsEffect(
                6, new Fixed(3), new CardTypePredicate(CardType.SORCERY), 2, true));
    }
}
