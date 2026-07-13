package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "6ED", collectorNumber = "312")
public class SnakeBasket extends Card {

    public SnakeBasket() {
        // {X}, Sacrifice this artifact: Create X 1/1 green Snake creature tokens. Activate only as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                false, "{X}",
                List.of(
                        new SacrificeSelfCost(),
                        new CreateTokenEffect(
                                new XValue(), "Snake", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.SNAKE), Set.of(), Set.of()
                        )
                ),
                "{X}, Sacrifice this artifact: Create X 1/1 green Snake creature tokens. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
