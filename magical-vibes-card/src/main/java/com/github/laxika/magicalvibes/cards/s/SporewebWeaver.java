package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M21", collectorNumber = "208")
public class SporewebWeaver extends Card {

    public SporewebWeaver() {
        // Whenever this creature is dealt damage, you gain 1 life and create a 1/1 green Saproling
        // creature token.
        addEffect(EffectSlot.ON_DEALT_DAMAGE, SequenceEffect.of(
                new GainLifeEffect(1),
                new CreateTokenEffect("Saproling", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.SAPROLING), Set.of(), Set.of())));
    }
}
