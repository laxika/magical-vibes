package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTargetCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YECL", collectorNumber = "11")
public class CraterousStomp extends Card {

    public CraterousStomp() {
        var creatureAnOpponentControls = TargetFilters.creatureAnOpponentControls().predicate();
        PermanentSubject otherCreaturesThatPlayerControls = Subjects.controlledBy(
                Subjects.controllerOf(Subjects.target()),
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsTargetCardPredicate()))));
        target(TargetFilters.creatureAnOpponentControls())
                .addSpellEffect(DealDamageEffect.toTargetCreature(3, creatureAnOpponentControls))
                .addSpellEffect(new GrantSubtypeUntilEndOfTurnEffect(
                        CardSubtype.COWARD, otherCreaturesThatPlayerControls, null, Duration.PERMANENT))
                .addSpellEffect(new GrantEffectToTargetEffect(
                        StaticAbility.of(new CanBlockOnlyIfAttackerMatchesPredicateEffect(
                                new PermanentNotPredicate(new PermanentHasAnySubtypePredicate(
                                        Set.of(CardSubtype.GIANT, CardSubtype.WARRIOR))),
                                "creatures that aren't Giants or Warriors")),
                        Duration.PERMANENT, false, otherCreaturesThatPlayerControls));
    }
}
