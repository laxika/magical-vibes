package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STH", collectorNumber = "129")
public class SliverQueen extends Card {

    public SliverQueen() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new CreateTokenEffect("Sliver", 1, 1, null,
                        List.of(CardSubtype.SLIVER), Set.of(), Set.of())),
                "{2}: Create a 1/1 colorless Sliver creature token."
        ));
    }
}
