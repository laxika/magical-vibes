package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VIS", collectorNumber = "143")
public class DiamondKaleidoscope extends Card {

    public DiamondKaleidoscope() {
        // {3}, {T}: Create a 0/1 colorless Prism artifact creature token.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new CreateTokenEffect("Prism", 0, 1, null,
                        List.of(CardSubtype.PRISM), Set.of(), Set.of(CardType.ARTIFACT))),
                "{3}, {T}: Create a 0/1 colorless Prism artifact creature token."
        ));

        // Sacrifice a Prism token: Add one mana of any color.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.PRISM),
                                new PermanentIsTokenPredicate())),
                                "Sacrifice a Prism token", false),
                        new AwardAnyColorManaEffect()),
                "Sacrifice a Prism token: Add one mana of any color."
        ));
    }
}
