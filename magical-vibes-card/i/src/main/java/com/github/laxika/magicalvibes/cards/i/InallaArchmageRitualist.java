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
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
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
        MayPayManaEffect copyWizard = new MayPayManaEffect(
                "{1}",
                new ConditionalEffect(
                        new AnyOf(List.of(new SourceIsOnBattlefield(), new SourceCardInCommandZone())),
                        new CreateTokenCopyOfTargetPermanentEffect(true, true)),
                "Pay {1} to create a token that's a copy of that Wizard?");
        CardEffect wizardTrigger = copyWizard;

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
                        new LoseLifeEffect(7, LoseLifeRecipient.TARGET_PLAYER)
                ),
                "Tap five untapped Wizards you control: Target player loses 7 life.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));
    }
}
