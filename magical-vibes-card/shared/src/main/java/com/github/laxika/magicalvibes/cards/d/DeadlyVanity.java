package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import java.util.List;

public class DeadlyVanity extends Card {

    public DeadlyVanity() {
        PermanentPredicate creatureOrPlaneswalker = new PermanentAnyOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsPlaneswalkerPredicate()));
        addSpellEffect(ChoosePermanentsEffect.exactly(1,
                Subjects.controlledBy(Subjects.eachPlayer(), creatureOrPlaneswalker),
                "a creature or planeswalker to keep"));
        addSpellEffect(DestroyAllPermanentsEffect.sparing(creatureOrPlaneswalker, Subjects.thisWay(StepKind.CHOOSE)));
    }
}
