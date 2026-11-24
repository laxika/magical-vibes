package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectYourDamageToTargetCreatureThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectYourDamageToTargetCreatureThisTurnEffect.RedirectedDamage;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "14")
@CardRegistration(set = "MKC", collectorNumber = "68")
public class GideonsSacrifice extends Card {

    public GideonsSacrifice() {
        addSpellEffect(ChoosePermanentsEffect.exactly(1, Subjects.controlledBy(Subjects.you(),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate()))), "a creature or planeswalker you control"));
        addSpellEffect(RedirectYourDamageToTargetCreatureThisTurnEffect
                .to(Subjects.thisWay(StepKind.CHOOSE), RedirectedDamage.YOU_AND_PERMANENTS)
                .orPlaneswalkerDestination());
    }
}
