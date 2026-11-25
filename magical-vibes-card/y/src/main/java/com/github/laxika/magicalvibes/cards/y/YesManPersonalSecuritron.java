package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "29")
@CardRegistration(set = "PIP", collectorNumber = "373")
@CardRegistration(set = "PIP", collectorNumber = "557")
@CardRegistration(set = "PIP", collectorNumber = "901")
public class YesManPersonalSecuritron extends Card {

    public YesManPersonalSecuritron() {
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                CreateTokenEffect.whiteSoldier(1).withTapped(true)
                        .withAmount(new CountersOnSource(CounterType.QUEST))
                        .forController(Subjects.ownerOf(Subjects.source())));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new TargetPlayerGainsControlOfSourceCreatureEffect(true,
                        new QueueReflexiveAbilityEffect(SequenceEffect.of(
                                new DrawCardEffect(2), PutCountersEffect.onSelf(CounterType.QUEST))))),
                "{T}: Target opponent gains control of Yes Man. When they do, you draw two cards and put a quest counter on Yes Man. Activate only during your turn.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"),
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ));
    }
}
