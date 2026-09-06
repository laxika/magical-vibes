package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "208")
@CardRegistration(set = "ONS", collectorNumber = "298")
public class WallOfMulch extends Card {

    public WallOfMulch() {
        addActivatedAbility(new ActivatedAbility(false, "{G}",
                List.of(new SacrificePermanentCost(new PermanentHasSubtypePredicate(CardSubtype.WALL),
                        "Sacrifice a Wall", false),
                        new DrawCardEffect(1)),
                "{G}, Sacrifice a Wall: Draw a card."));
    }
}
