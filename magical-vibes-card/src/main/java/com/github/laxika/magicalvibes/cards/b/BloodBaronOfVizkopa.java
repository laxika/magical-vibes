package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.AnOpponentLifeAtMost;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DGM", collectorNumber = "57")
public class BloodBaronOfVizkopa extends Card {

    public BloodBaronOfVizkopa() {
        // Protection from white and from black.
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.WHITE, CardColor.BLACK)));
        // As long as you have 30 or more life and an opponent has 10 or less life, this creature gets +6/+6 and has flying.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AllConditions(List.of(new ControllerLifeAtLeast(30), new AnOpponentLifeAtMost(10))),
                new StaticBoostEffect(6, 6, Set.of(Keyword.FLYING), GrantScope.SELF)));
    }
}
