package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.SourceCardInCommandZone;
import com.github.laxika.magicalvibes.model.condition.SourceIsOnBattlefield;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "FCA", collectorNumber = "52")
public class InallaArchmageRitualist extends Card {

    public InallaArchmageRitualist() {
        MayPayManaEffect copyWizard = new MayPayManaEffect(
                "{1}",
                new ConditionalEffect(
                        new AnyOf(List.of(new SourceIsOnBattlefield(), new SourceCardInCommandZone())),
                        new CreateTokenCopyOfTargetPermanentEffect(true, true)),
                "Pay {1} to create a token that's a copy of that Wizard?");
        TriggeringCardConditionalEffect wizardTrigger = new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.WIZARD), copyWizard);

        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD, wizardTrigger);
        addEffect(EffectSlot.COMMAND_ZONE_ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD, wizardTrigger);

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
