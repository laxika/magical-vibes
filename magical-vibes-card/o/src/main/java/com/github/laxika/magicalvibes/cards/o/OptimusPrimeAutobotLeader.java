package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.effect.BolsterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToChosenCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToChosenCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformGrantingPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/** Back face of Optimus Prime, Hero. */
public class OptimusPrimeAutobotLeader extends Card {

    public OptimusPrimeAutobotLeader() {
        addStaticEffect(new ConditionalEffect(
                new NotCondition(new NotControllerTurn()),
                new GrantCardTypeEffect(CardType.CREATURE, GrantScope.SELF)));

        addTrigger(Triggers.youAttack(), SequenceEffect.of(
                new BolsterEffect(2, true),
                new GrantKeywordToChosenCreatureUntilEndOfTurnEffect(Keyword.TRAMPLE, null),
                new GrantEffectToChosenCreatureUntilEndOfTurnEffect(
                        new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                        new TransformGrantingPermanentEffect()))));
    }
}
