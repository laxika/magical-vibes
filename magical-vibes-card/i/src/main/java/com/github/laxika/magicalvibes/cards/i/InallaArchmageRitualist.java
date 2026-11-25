package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.SourceCardInCommandZone;
import com.github.laxika.magicalvibes.model.condition.SourceCardOnBattlefield;
import com.github.laxika.magicalvibes.model.condition.SourceIsOnBattlefield;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1639")
@CardRegistration(set = "FCA", collectorNumber = "52")
@CardRegistration(set = "C17", collectorNumber = "38")
public class InallaArchmageRitualist extends Card {

    public InallaArchmageRitualist() {
        // "If Inalla is in the command zone or on the battlefield" is an intervening "if" (CR 603.4): with it false as
        // the ability resolves, nothing is asked.
        CardEffect wizardTrigger = new ConditionalEffect(
                new AnyOf(List.of(new SourceIsOnBattlefield(), new SourceCardInCommandZone())),
                UnlessPaysEffect.youMayPay("{1}",
                                new CreateTokenCopyOfTargetPermanentEffect(true, true).ofTriggeringPermanent())
                        .withPrompt("Pay {1} to create a token that's a copy of that Wizard?"));

        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.NONTOKEN_CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.WIZARD))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                wizardTrigger);
        addTrigger(new TriggerCondition(Triggers.enters(
                                new PermanentAllOfPredicate(List.of(
                                        Triggers.NONTOKEN_CREATURE,
                                        new PermanentHasSubtypePredicate(CardSubtype.WIZARD))),
                                ObjectRelation.YOU_CONTROL),
                        AbilityZones.COMMAND),
                TriggerOptions.DEFAULT,
                wizardTrigger);

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(5,
                                new PermanentHasSubtypePredicate(CardSubtype.WIZARD)),
                        new LoseLifeEffect(7, Subjects.targetPlayer())
                ),
                "Tap five untapped Wizards you control: Target player loses 7 life.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));
    }
}
