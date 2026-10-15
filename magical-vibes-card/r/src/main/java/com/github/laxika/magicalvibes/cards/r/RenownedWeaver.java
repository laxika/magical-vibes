package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "JOU", collectorNumber = "137")
public class RenownedWeaver extends Card {

    public RenownedWeaver() {
        // {1}{G}, Sacrifice this creature: Create a 1/3 green Spider enchantment creature token with reach.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(
                        new SacrificeSelfCost(),
                        new CreateTokenEffect(
                                "Spider", 1, 3, CardColor.GREEN,
                                List.of(CardSubtype.SPIDER), Set.of(Keyword.REACH),
                                Set.of(CardType.ENCHANTMENT)
                        )
                ),
                "{1}{G}, Sacrifice Renowned Weaver: Create a 1/3 green Spider enchantment creature token with reach."
        ));
    }
}
