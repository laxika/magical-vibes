package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "253")
public class Fountainport extends Card {

    public Fountainport() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsTokenPredicate(), "Sacrifice a token", false),
                        new DrawCardEffect(1)
                ),
                "{2}, {T}, Sacrifice a token: Draw a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new PayLifeCost(1),
                        new CreateTokenEffect("Fish", 1, 1, CardColor.BLUE,
                                List.of(CardSubtype.FISH), Set.of(), Set.of())
                ),
                "{3}, {T}, Pay 1 life: Create a 1/1 blue Fish creature token."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(CreateTokenEffect.ofTreasureToken(1)),
                "{4}, {T}: Create a Treasure token."
        ));
    }
}
