package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MillBottomOfTargetLibraryConditionalTokenEffect;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "218")
public class CellarDoor extends Card {

    public CellarDoor() {
        addActivatedAbility(new ActivatedAbility(
                true, "{3}",
                List.of(new MillBottomOfTargetLibraryConditionalTokenEffect(
                        CardType.CREATURE,
                        "Zombie",
                        2, 2,
                        CardColor.BLACK,
                        List.of(CardSubtype.ZOMBIE)
                )),
                "{3}, {T}: Target player puts the bottom card of their library into their graveyard. If it's a creature card, you create a 2/2 black Zombie creature token."
        ));
    }
}
