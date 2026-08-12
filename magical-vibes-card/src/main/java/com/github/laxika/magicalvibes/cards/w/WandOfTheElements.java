package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DST", collectorNumber = "158")
public class WandOfTheElements extends Card {

    public WandOfTheElements() {
        // {T}, Sacrifice an Island: Create a 2/2 blue Elemental creature token with flying.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.ISLAND), "Sacrifice an Island"),
                        new CreateTokenEffect("Elemental", 2, 2, CardColor.BLUE,
                                List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.FLYING), Set.of())
                ),
                "{T}, Sacrifice an Island: Create a 2/2 blue Elemental creature token with flying."
        ));

        // {T}, Sacrifice a Mountain: Create a 3/3 red Elemental creature token.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN), "Sacrifice a Mountain"),
                        new CreateTokenEffect("Elemental", 3, 3, CardColor.RED,
                                List.of(CardSubtype.ELEMENTAL), Set.of(), Set.of())
                ),
                "{T}, Sacrifice a Mountain: Create a 3/3 red Elemental creature token."
        ));
    }
}
