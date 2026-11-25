package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M15", collectorNumber = "95")
@CardRegistration(set = "IMA", collectorNumber = "89")
@CardRegistration(set = "ANB", collectorNumber = "49")
public class EternalThirst extends Card {

    public EternalThirst() {
        target(TargetFilters.creature())
                .addStaticEffect(new GrantKeywordEffect(Keyword.LIFELINK, Subjects.attachedCreature()))
                .addStaticEffect(new GrantTriggeredAbilityEffect(
                                new TriggeredAbility(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS),
                                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)),
                                Subjects.attachedCreature()));
    }
}
