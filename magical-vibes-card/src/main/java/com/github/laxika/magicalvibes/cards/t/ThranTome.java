package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ThranTomeRevealTopThreeOpponentChoosesEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "160")
public class ThranTome extends Card {

    public ThranTome() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(
                        new ThranTomeRevealTopThreeOpponentChoosesEffect(),
                        new DrawCardEffect(2)
                ),
                "{5}, {T}: Reveal the top three cards of your library. Target opponent chooses one of those cards. "
                        + "Put that card into your graveyard, then draw two cards.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "You must target an opponent."
                )
        ));
    }
}
