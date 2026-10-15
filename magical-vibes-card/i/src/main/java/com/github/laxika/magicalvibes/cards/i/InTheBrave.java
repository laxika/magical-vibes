package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerHasEnduringStory;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.StoriedEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "106")
public class InTheBrave extends Card {

    public InTheBrave() {
        addEffect(EffectSlot.STATIC, new StoriedEffect());
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerHasEnduringStory(),
                new StaticBoostEffect(1, 0, Set.of(Keyword.HASTE), GrantScope.SELF)));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new DrawCardEffect(1)
                ),
                "{1}, {T}, Discard a card: Draw a card."
        ));
    }
}
