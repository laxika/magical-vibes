package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.condition.SourceCardOnBattlefield;
import com.github.laxika.magicalvibes.model.condition.SourcePowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.AllowCastSourceCardFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YONE", collectorNumber = "23")
public class IchorAberration extends Card {

    public IchorAberration() {
        addStaticEffect(new ConditionalEffect(
                new SourcePowerAtLeast(7),
                new CanAttackAsThoughNoDefenderEffect()));

        Condition inGraveyardOrOnBattlefield = new AnyOf(List.of(
                new SourceCardOnBattlefield(), new SourceCardInGraveyard()));
        CardEffect proliferateEffect = new ConditionalEffect(inGraveyardOrOnBattlefield, SequenceEffect.of(
                new PerpetuallyBoostSourceEffect(1, 1),
                new AllowCastSourceCardFromGraveyardThisTurnEffect()));
        addTrigger(Triggers.youProliferate(), proliferateEffect);
        addTrigger(new TriggerCondition(Triggers.youProliferate(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT, proliferateEffect);
    }
}
