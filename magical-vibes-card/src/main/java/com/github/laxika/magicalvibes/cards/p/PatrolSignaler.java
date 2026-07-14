package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EVE", collectorNumber = "12")
public class PatrolSignaler extends Card {

    public PatrolSignaler() {
        // {1}{W}, {Q}: Create a 1/1 white Kithkin Soldier creature token.
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{W}",
                List.of(new CreateTokenEffect(
                        "Kithkin Soldier", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.KITHKIN, CardSubtype.SOLDIER),
                        Set.of(), Set.of())),
                "{1}{W}, {Q}: Create a 1/1 white Kithkin Soldier creature token."
        ).withRequiresUntap());
    }
}
