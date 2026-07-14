package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DyingCreatureControllerDiscardsCardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

@CardRegistration(set = "7ED", collectorNumber = "120")
public class Bereavement extends Card {

    public Bereavement() {
        // Whenever a green creature dies, its controller discards a card.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardColorPredicate(CardColor.GREEN),
                new DyingCreatureControllerDiscardsCardEffect()));
    }
}
