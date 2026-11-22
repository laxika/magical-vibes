package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "82")
public class TreacherousUrge extends Card {

    public TreacherousUrge() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addSpellEffect(RevealCardsFromHandEffect.hand(Subjects.targetPlayer()))
                .addSpellEffect(ChooseCardEffect.upTo(
                        Subjects.revealedThisWay(new CardTypePredicate(CardType.CREATURE)), 1,
                        "a creature card to put onto the battlefield under your control"))
                .addSpellEffect(new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE))
                        .underControlOf(Subjects.you())
                        .enteringWith(new GrantKeywordEffect(Keyword.HASTE, Subjects.target(), Duration.PERMANENT)))
                .addSpellEffect(RegisterDelayedStepTriggerEffect.aboutPutOntoBattlefield(
                        Triggers.endStep(ActivePlayer.ANY), SacrificeSelfEffect.ofTriggeringPermanent()));
    }
}
