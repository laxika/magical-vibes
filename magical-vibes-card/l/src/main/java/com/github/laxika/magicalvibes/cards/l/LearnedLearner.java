package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerMaximumHandSizeOtherThanSeven;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "MB1", collectorNumber = "25")
public class LearnedLearner extends Card {

    public LearnedLearner() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerMaximumHandSizeOtherThanSeven(),
                new GrantActivatedAbilityEffect(
                        new ActivatedAbility(true, null, List.of(new DrawCardEffect()), "{T}: Draw a card."),
                        GrantScope.SELF)));
    }
}
