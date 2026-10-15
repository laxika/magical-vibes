package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HBG", collectorNumber = "190")
public class SwashbucklerExtraordinaire extends Card {

    public SwashbucklerExtraordinaire() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofTreasureToken(1));

        GrantKeywordEffect grantDoubleStrike =
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGETS);
        var targetGroup = targetUpTo(new EventValue(), TargetFilters.creature(), 100);
        targetGroup.addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                SequenceEffect.of(
                        new SacrificeAnyNumberOfPermanentsEffect(
                                new PermanentHasSubtypePredicate(CardSubtype.TREASURE)),
                        ConditionalEffect.unless(
                                new EventValueAtLeast(1),
                                new QueueReflexiveAbilityEffect(grantDoubleStrike, false, true))),
                "Sacrifice one or more Treasures?"));
    }
}
