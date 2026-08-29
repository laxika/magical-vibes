package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaPairsSpentToCast;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "229")
public class BladecoilSerpent extends Card {

    public BladecoilSerpent() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new ColorSpentToCast(ManaColor.BLUE, 2),
                new DrawCardEffect(new ColorManaPairsSpentToCast(ManaColor.BLUE))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new ColorSpentToCast(ManaColor.BLACK, 2),
                new DiscardEffect(new ColorManaPairsSpentToCast(ManaColor.BLACK), DiscardRecipient.EACH_OPPONENT)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new ColorSpentToCast(ManaColor.RED, 2),
                SequenceEffect.of(
                        new BoostSelfEffect(new ColorManaPairsSpentToCast(ManaColor.RED), new Fixed(0)),
                        new GrantKeywordEffect(Set.of(Keyword.TRAMPLE, Keyword.HASTE), GrantScope.SELF))));
    }
}
