package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtFaceDownCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "197")
public class RevealingWind extends Card {

    public RevealingWind() {
        addSpellEffect(PreventDamageEffect.allCombat());
        // "You may look at each face-down creature that's attacking or blocking."
        addSpellEffect(new LookAtFaceDownCreatureEffect(Subjects.controlledBy(Subjects.eachPlayer(),
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsFaceDownPredicate(),
                        new PermanentIsCreaturePredicate(),
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsAttackingPredicate(), new PermanentIsBlockingPredicate())))))));
    }
}
