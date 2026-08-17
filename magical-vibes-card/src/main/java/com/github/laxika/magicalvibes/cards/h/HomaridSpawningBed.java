package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FEM", collectorNumber = "21")
public class HomaridSpawningBed extends Card {

    public HomaridSpawningBed() {
        var blueCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentColorInPredicate(Set.of(CardColor.BLUE))));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}{U}",
                List.of(
                        new SacrificePermanentCost(blueCreature, "a blue creature", false, false, true, false),
                        new CreateTokenEffect(new XValue(), "Camarid", 1, 1,
                                CardColor.BLUE, List.of(CardSubtype.CAMARID), Set.of(), Set.of())
                ),
                "{1}{U}{U}, Sacrifice a blue creature: Create X 1/1 blue Camarid creature tokens, "
                        + "where X is the sacrificed creature's mana value."
        ));
    }
}
