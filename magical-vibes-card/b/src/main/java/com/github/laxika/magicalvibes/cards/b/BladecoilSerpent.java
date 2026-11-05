package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
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
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "229")
public class BladecoilSerpent extends Card {

    public BladecoilSerpent() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DrawCardEffect(new ColorManaPairsSpentToCast(ManaColor.BLUE)));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DiscardEffect(new ColorManaPairsSpentToCast(ManaColor.BLACK), DiscardRecipient.EACH_OPPONENT));
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new BoostSelfEffect(new ColorManaPairsSpentToCast(ManaColor.RED), new Fixed(0)),
                new ConditionalEffect(new ColorSpentToCast(ManaColor.RED, 2),
                        new GrantKeywordEffect(Set.of(Keyword.TRAMPLE, Keyword.HASTE), GrantScope.SELF))));
    }
}
