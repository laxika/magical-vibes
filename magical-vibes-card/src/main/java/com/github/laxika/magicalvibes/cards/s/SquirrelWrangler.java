package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PCY", collectorNumber = "127")
public class SquirrelWrangler extends Card {

    public SquirrelWrangler() {
        // {1}{G}, Sacrifice a land: Create two 1/1 green Squirrel creature tokens.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsLandPredicate(), "a land", false),
                        new CreateTokenEffect(2, "Squirrel", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.SQUIRREL), Set.of(), Set.of())
                ),
                "{1}{G}, Sacrifice a land: Create two 1/1 green Squirrel creature tokens."
        ));

        // {1}{G}, Sacrifice a land: Squirrel creatures get +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsLandPredicate(), "a land", false),
                        new BoostAllCreaturesEffect(1, 1,
                                new PermanentHasSubtypePredicate(CardSubtype.SQUIRREL))
                ),
                "{1}{G}, Sacrifice a land: Squirrel creatures get +1/+1 until end of turn."
        ));
    }
}
