package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerHandFaceDownWithSourceAndReturnAtNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PlayFromOutsideHandTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "70")
@CardRegistration(set = "ACR", collectorNumber = "122")
public class AppleOfEdenIsuRelic extends Card {

    public AppleOfEdenIsuRelic() {
        List<CardEffect> ownerDrawTrigger = List.of(
                new DrawCardForTargetPlayerEffect(1));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayLifeCost(4),
                        new SacrificeSelfCost(),
                        new ExileTargetPlayerHandFaceDownWithSourceAndReturnAtNextEndStepEffect(),
                        new AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect(null),
                        new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                                EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                                new PlayFromOutsideHandTriggerEffect(ownerDrawTrigger)),
                        new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                                EffectSlot.ON_CONTROLLER_PLAYS_LAND,
                                new PlayFromOutsideHandTriggerEffect(ownerDrawTrigger))
                ),
                "{T}, Pay 4 life, Sacrifice Apple of Eden: Look at target opponent's hand and exile "
                        + "those cards face down. You may play those cards this turn, and mana of any "
                        + "type can be spent to cast them. Until end of turn, whenever you play a land "
                        + "or cast a spell this way, its owner draws a card. At the beginning of the "
                        + "next end step, return the exiled cards to their owner's hand. Activate only "
                        + "as a sorcery.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
