package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.condition.SacrificedCardMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.CardSubject;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1199")
public class TeveshSzatDoomOfFools extends Card {

    private static final PermanentAllOfPredicate SACRIFICE_FILTER = new PermanentAllOfPredicate(List.of(
            new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()),
            new PermanentAnyOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentIsPlaneswalkerPredicate()))));

    public TeveshSzatDoomOfFools() {
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new CreateTokenEffect(
                        2, "Thrull", 0, 1, CardColor.BLACK,
                        List.of(CardSubtype.THRULL), Set.of(), Set.of())),
                "+2: Create two 0/1 black Thrull creature tokens."
        ));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new MayEffect(
                        new SacrificePermanentThenEffect(
                                SACRIFICE_FILTER,
                                SequenceEffect.of(
                                        new DrawCardEffect(2),
                                        new ConditionalEffect(
                                                SacrificedCardMatches.permanent(
                                                        new PermanentIsCommanderPredicate(), "a commander"),
                                                new DrawCardEffect(1))),
                                "another creature or planeswalker"),
                        "Sacrifice another creature or planeswalker?")),
                "+1: You may sacrifice another creature or planeswalker. If you do, draw two cards, then draw another card if the sacrificed permanent was a commander."
        ));

        addActivatedAbility(new ActivatedAbility(
                -10,
                List.of(GainControlOfTargetEffect.of(
                                Subjects.controlledBy(Subjects.eachPlayer(), new PermanentIsCommanderPredicate()),
                                Duration.PERMANENT),
                        new PutCardsOntoBattlefieldEffect(new CardSubject.InCommandZone(Subjects.eachPlayer(),
                                new CardIsCommanderPredicate())).underControlOf(Subjects.you())),
                "−10: Gain control of all commanders. Put all commanders from the command zone onto the battlefield under your control."
        ));
    }
}
