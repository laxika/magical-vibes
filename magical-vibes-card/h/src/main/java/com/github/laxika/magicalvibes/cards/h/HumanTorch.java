package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.condition.AttackedTargetIsOpponent;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "3")
public class HumanTorch extends Card {

    public HumanTorch() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new ControllerCastSpellThisTurn(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)))),
                new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.DOUBLE_STRIKE, Keyword.HASTE), Subjects.source()));

        addTrigger(Triggers.attacks(), UnlessPaysEffect.youMayPay("{R}{G}{W}{U}", new GrantEffectToTargetUntilEndOfTurnEffect(
                        new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(), TriggerOptions.DEFAULT.withEventCondition(new AttackedTargetIsOpponent()),
                        DealDamageEffect.to(Subjects.except(Subjects.eachOpponent(), Subjects.triggeringPlayer()),
                                new EventValue())), Subjects.source())).withPrompt("Pay {R}{G}{W}{U} to deal that much damage to each other opponent?"));
    }
}
