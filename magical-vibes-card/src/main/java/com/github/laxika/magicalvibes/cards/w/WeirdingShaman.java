package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOR", collectorNumber = "84")
public class WeirdingShaman extends Card {

    public WeirdingShaman() {
        // {3}{B}, Sacrifice a Goblin: Create two 1/1 black Goblin Rogue creature tokens.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasSubtypePredicate(CardSubtype.GOBLIN))),
                                "Sacrifice a Goblin", false),
                        new CreateTokenEffect(
                                2, "Goblin Rogue", 1, 1, CardColor.BLACK,
                                List.of(CardSubtype.GOBLIN, CardSubtype.ROGUE),
                                Set.of(), Set.of())),
                "{3}{B}, Sacrifice a Goblin: Create two 1/1 black Goblin Rogue creature tokens."
        ));
    }
}
