package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.OpponentChoosesOneOfTopTwoGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "301")
public class PhyrexianGrimoire extends Card {

    public PhyrexianGrimoire() {
        // {4}, {T}: Target opponent chooses one of the top two cards of your graveyard.
        // Exile that card and put the other one into your hand.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new OpponentChoosesOneOfTopTwoGraveyardCardsEffect()),
                "{4}, {T}: Target opponent chooses one of the top two cards of your graveyard. "
                        + "Exile that card and put the other one into your hand.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "You must target an opponent.")));
    }
}
