package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "70")
@CardRegistration(set = "ACR", collectorNumber = "122")
public class AppleOfEdenIsuRelic extends Card {

    public AppleOfEdenIsuRelic() {
        CardEffect ownerDraws = new DrawCardEffect(1, Subjects.ownerOf(Subjects.triggeringCard()));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayLifeCost(4),
                        new SacrificeSelfCost(),
                        new ExileTargetPlayerHandEffect(true).exiledFaceDown(),
                        RegisterDelayedStepTriggerEffect.aboutExiled(Triggers.endStep(ActivePlayer.ANY),
                                new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.EXILE))),
                        new AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect(null),
                        new RegisterGlobalTriggeredAbilityEffect(
                                new TriggeredAbility(Triggers.youCastSpell().exiledWithThis(), ownerDraws)),
                        new RegisterGlobalTriggeredAbilityEffect(
                                new TriggeredAbility(Triggers.youPlayLand().exiledWithThis(), ownerDraws))
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
