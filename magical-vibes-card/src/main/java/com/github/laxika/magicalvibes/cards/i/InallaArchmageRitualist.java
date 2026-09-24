package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.SourceCardInCommandZone;
import com.github.laxika.magicalvibes.model.condition.SourceCardOnBattlefield;
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

@CardRegistration(set = "SLD", collectorNumber = "1639")
public class InallaArchmageRitualist extends Card {

    public InallaArchmageRitualist() {
        // Eminence — Whenever another nontoken Wizard you control enters, if Inalla is in the
        // command zone or on the battlefield, you may pay {1}. If you do, create a token that's a
        // copy of that Wizard. The token gains haste. Exile it at the beginning of the next end step.
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.WIZARD),
                        new MayPayManaEffect(
                                "{1}",
                                new ConditionalEffect(
                                        new AnyOf(List.of(
                                                new SourceCardInCommandZone(),
                                                new SourceCardOnBattlefield())),
                                        new CreateTokenCopyOfTargetPermanentEffect(true, true)),
                                "Pay {1} to create a hasty token copy of that Wizard (exiled at the beginning of the next end step)?"
                        )));
        addEffect(EffectSlot.COMMAND_ZONE_ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.WIZARD),
                        new MayPayManaEffect(
                                "{1}",
                                new ConditionalEffect(
                                        new AnyOf(List.of(
                                                new SourceCardInCommandZone(),
                                                new SourceCardOnBattlefield())),
                                        new CreateTokenCopyOfTargetPermanentEffect(true, true)),
                                "Pay {1} to create a hasty token copy of that Wizard (exiled at the beginning of the next end step)?"
                        )));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(5, new PermanentHasSubtypePredicate(CardSubtype.WIZARD)),
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
