package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ControllerSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "81")
public class StitchersApprentice extends Card {

    public StitchersApprentice() {
        // {1}{U}, {T}: Create a 2/2 blue Homunculus creature token, then sacrifice a creature.
        addActivatedAbility(new ActivatedAbility(
                true, "{1}{U}",
                List.of(
                        new CreateTokenEffect(
                                "Homunculus", 2, 2,
                                CardColor.BLUE,
                                List.of(CardSubtype.HOMUNCULUS),
                                Set.of(),
                                Set.of()
                        ),
                        new ControllerSacrificesCreatureEffect()
                ),
                "{1}{U}, {T}: Create a 2/2 blue Homunculus creature token, then sacrifice a creature."
        ));
    }
}
