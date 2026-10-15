package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "101")
public class GoblinLookout extends Card {

    public GoblinLookout() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.GOBLIN),
                                "Sacrifice a Goblin",
                                false
                        ),
                        new BoostAllCreaturesEffect(2, 0,
                                new PermanentHasSubtypePredicate(CardSubtype.GOBLIN))
                ),
                "{T}, Sacrifice a Goblin: Goblin creatures get +2/+0 until end of turn."
        ));
    }
}
