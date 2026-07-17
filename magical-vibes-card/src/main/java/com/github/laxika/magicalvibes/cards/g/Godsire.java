package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALA", collectorNumber = "170")
public class Godsire extends Card {

    public Godsire() {
        // {T}: Create an 8/8 Beast creature token that's red, green, and white.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new CreateTokenEffect("Beast", 8, 8, CardColor.RED,
                        Set.of(CardColor.RED, CardColor.GREEN, CardColor.WHITE),
                        List.of(CardSubtype.BEAST))),
                "{T}: Create an 8/8 Beast creature token that's red, green, and white."));
    }
}
