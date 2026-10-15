package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "220")
public class ScrollOfAvacyn extends Card {

    public ScrollOfAvacyn() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificeSelfCost(),
                        new DrawCardEffect(),
                        new ConditionalEffect(
                                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.ANGEL)),
                                new GainLifeEffect(5)
                        )
                ),
                "{1}, Sacrifice Scroll of Avacyn: Draw a card. If you control an Angel, you gain 5 life."
        ));
    }
}
