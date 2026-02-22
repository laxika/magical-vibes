package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenWithColorsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "10")
public class ClachanFestival extends Card {

    public ClachanFestival() {
        // When this enchantment enters, create two 1/1 green and white Kithkin creature tokens.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateCreatureTokenWithColorsEffect(
                2,
                "Kithkin", 1, 1,
                Set.of(CardColor.GREEN, CardColor.WHITE),
                CardColor.WHITE,
                List.of(CardSubtype.KITHKIN)
        ));

        // {4}{W}: Create a 1/1 green and white Kithkin creature token.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{W}",
                List.of(new CreateCreatureTokenWithColorsEffect(
                        "Kithkin", 1, 1,
                        Set.of(CardColor.GREEN, CardColor.WHITE),
                        CardColor.WHITE,
                        List.of(CardSubtype.KITHKIN)
                )),
                false,
                "{4}{W}: Create a 1/1 green and white Kithkin creature token."
        ));
    }
}
