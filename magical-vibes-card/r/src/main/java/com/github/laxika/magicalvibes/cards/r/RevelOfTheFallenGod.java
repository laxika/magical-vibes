package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "JOU", collectorNumber = "155")
public class RevelOfTheFallenGod extends Card {

    public RevelOfTheFallenGod() {
        // Create four 2/2 red and green Satyr creature tokens with haste.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                4, "Satyr", 2, 2, CardColor.RED, Set.of(CardColor.RED, CardColor.GREEN),
                List.of(CardSubtype.SATYR), Set.of(Keyword.HASTE), Set.of()));
    }
}
