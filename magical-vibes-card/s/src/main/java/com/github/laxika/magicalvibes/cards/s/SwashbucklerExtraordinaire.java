package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HBG", collectorNumber = "190")
public class SwashbucklerExtraordinaire extends Card {

    public SwashbucklerExtraordinaire() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.ofTreasureToken(1));

        GrantKeywordEffect grantDoubleStrike =
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, Subjects.triggeringSpellTargets());
        var targetGroup = targetUpTo(new AmountThisWay(StepKind.SACRIFICE), TargetFilters.creature(), 100);
        targetGroup.addTrigger(Triggers.youAttack(), new MayEffect(
                SequenceEffect.of(
                        SacrificePermanentsEffect.anyNumber(
                                new PermanentHasSubtypePredicate(CardSubtype.TREASURE), Subjects.you()),
                        new ConditionalEffect(
                                HappenedThisWay.sacrificed(),
                                new QueueReflexiveAbilityEffect(grantDoubleStrike))),
                "Sacrifice one or more Treasures?"));
    }
}
