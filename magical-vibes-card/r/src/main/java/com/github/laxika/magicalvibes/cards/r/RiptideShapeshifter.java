package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealUntilChosenCreatureTypeToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "109")
public class RiptideShapeshifter extends Card {

    public RiptideShapeshifter() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}{U}",
                List.of(new SacrificeSelfCost(), new RevealUntilChosenCreatureTypeToBattlefieldEffect()),
                "{2}{U}{U}, Sacrifice this creature: Choose a creature type. Reveal cards from the top of your library until you reveal a creature card of that type. Put that card onto the battlefield and shuffle the rest into your library."
        ));
    }
}
