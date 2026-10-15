package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "108")
public class IlluminateHistory extends Card {

    public IlluminateHistory() {
        addEffect(EffectSlot.SPELL, new DiscardUpToThenDrawThatManyEffect(
                DiscardUpToThenDrawThatManyEffect.ANY_NUMBER));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new GraveyardCardThreshold(7, null),
                new CreateTokenEffect("Spirit", 3, 2, CardColor.RED,
                        Set.of(CardColor.RED, CardColor.WHITE), List.of(CardSubtype.SPIRIT))));
    }
}
