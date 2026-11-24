package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSharesCardTypeWithPermanentsPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "82")
public class Reweave extends Card {

    public Reweave() {
        // "If the player does, they reveal ... until they reveal a permanent card that shares a card type with the
        // sacrificed permanent": the types it had as it was sacrificed (CR 608.2h).
        target(TargetFilters.permanent())
                .addSpellEffect(new SacrificeSelfEffect(Subjects.target(), Subjects.controllerOf(Subjects.target()),
                        new PermanentTruePredicate()))
                .addSpellEffect(new ConditionalEffect(HappenedThisWay.sacrificed(),
                        RevealUntilCardPredicateRestOnBottomRandomEffect.ontoBattlefield(
                                new CardAllOfPredicate(List.of(new CardIsPermanentPredicate(),
                                        new CardSharesCardTypeWithPermanentsPredicate(
                                                Subjects.departedThisWay(StepKind.SACRIFICE)))),
                                new RevealUntilCardPredicateRestOnBottomRandomEffect.Players(
                                        Subjects.controllerOf(Subjects.target()))).shufflingRest()));

        // Splice onto Arcane {2}{U}{U}
        addStaticEffect(new SpliceEffect(CardSubtype.ARCANE, "{2}{U}{U}"));
    }
}
