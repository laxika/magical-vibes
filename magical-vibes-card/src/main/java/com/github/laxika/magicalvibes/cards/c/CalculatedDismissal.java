package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "48")
public class CalculatedDismissal extends Card {

    public CalculatedDismissal() {
        // Spell mastery — If there are two or more instant and/or sorcery cards in your graveyard, scry 2.
        //
        // The scry is listed before the counter because the two instructions are independent (the scry
        // happens whether or not the spell is countered), and the counter hands off to the pay-or-counter
        // choice flow.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new GraveyardCardThreshold(2, new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)
        ))), new ScryEffect(2)));

        // Counter target spell unless its controller pays {3}.
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(3));
    }
}
