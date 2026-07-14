package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardXAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeXPermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EVE", collectorNumber = "179")
public class SpringjackPasture extends Card {

    public SpringjackPasture() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));

        // {4}, {T}: Create a 0/1 white Goat creature token.
        addActivatedAbility(new ActivatedAbility(
                true, "{4}",
                List.of(new CreateTokenEffect("Goat", 0, 1, CardColor.WHITE,
                        List.of(CardSubtype.GOAT), Set.of(), Set.of())),
                "{4}, {T}: Create a 0/1 white Goat creature token."
        ));

        // {T}, Sacrifice X Goats: Add X mana of any one color. You gain X life.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(
                        new SacrificeXPermanentsCost(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.GOAT)
                        ))),
                        new AwardXAnyColorManaEffect(),
                        new GainLifeEffect(new XValue())
                ),
                "{T}, Sacrifice X Goats: Add X mana of any one color. You gain X life."
        ));
    }
}
