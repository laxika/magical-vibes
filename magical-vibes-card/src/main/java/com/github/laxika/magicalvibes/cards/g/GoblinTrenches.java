package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "APC", collectorNumber = "104")
public class GoblinTrenches extends Card {

    public GoblinTrenches() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsLandPredicate(), "Sacrifice a land"),
                        new CreateTokenEffect(2, "Goblin Soldier", 1, 1,
                                CardColor.RED, Set.of(CardColor.RED, CardColor.WHITE),
                                List.of(CardSubtype.GOBLIN, CardSubtype.SOLDIER))
                ),
                "{2}, Sacrifice a land: Create two 1/1 red and white Goblin Soldier creature tokens."
        ));
    }
}
