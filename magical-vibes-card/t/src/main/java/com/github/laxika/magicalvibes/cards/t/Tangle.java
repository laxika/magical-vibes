package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "213")
@CardRegistration(set = "VMA", collectorNumber = "237")
@CardRegistration(set = "MAR", collectorNumber = "38")
@CardRegistration(set = "OMB", collectorNumber = "38")
public class Tangle extends Card {

    public Tangle() {
        addSpellEffect(PreventDamageEffect.allCombat());
        addSpellEffect(new SkipNextUntapEffect(
                Subjects.controlledBy(Subjects.eachPlayer(), new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(), new PermanentIsAttackingPredicate())))));
    }
}
