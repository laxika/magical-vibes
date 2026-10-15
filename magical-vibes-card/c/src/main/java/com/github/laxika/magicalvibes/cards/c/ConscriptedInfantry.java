package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "129")
public class ConscriptedInfantry extends Card {

    public ConscriptedInfantry() {
        // When this creature dies, create a 1/1 colorless Soldier artifact creature token.
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                "Soldier", 1, 1, (CardColor) null,
                List.of(CardSubtype.SOLDIER), Set.of(), Set.of(CardType.ARTIFACT)));
    }
}
