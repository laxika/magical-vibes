package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SeekCardsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "YOTJ", collectorNumber = "25")
public class SilentExtraction extends Card {

    public SilentExtraction() {
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new GraveyardCardThreshold(7, null),
                new SeekCardsToHandEffect(
                        new Fixed(1),
                        new CardTypePredicate(CardType.CREATURE),
                        new ManaValueBound(new TargetSpellManaValue(), true, 0))));
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(2));
    }
}
