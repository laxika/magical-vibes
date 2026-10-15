package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "15")
public class MassProduction extends Card {

    public MassProduction() {
        // Create four 1/1 colorless Soldier artifact creature tokens.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(4, "Soldier", 1, 1, null,
                List.of(CardSubtype.SOLDIER), Set.of(), Set.of(CardType.ARTIFACT)));
    }
}
