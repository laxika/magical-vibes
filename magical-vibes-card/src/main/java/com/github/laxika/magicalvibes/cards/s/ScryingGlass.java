package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseColorForSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePositiveNumberEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTargetHandDrawIfExactlyChosenNumberOfChosenColorEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "UDS", collectorNumber = "137")
public class ScryingGlass extends Card {

    public ScryingGlass() {
        addActivatedAbility(new ActivatedAbility(
                true, "{3}",
                List.of(
                        new ChoosePositiveNumberEffect(),
                        new ChooseColorForSourceEffect(),
                        new RevealTargetHandDrawIfExactlyChosenNumberOfChosenColorEffect()
                ),
                "{3}, {T}: Choose a number greater than 0 and a color. Target opponent reveals their hand. "
                        + "If that opponent reveals exactly the chosen number of cards of the chosen color, you draw a card.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));
    }
}
