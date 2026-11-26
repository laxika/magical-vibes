package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "110")
public class LavaStorm extends Card {

    public LavaStorm() {
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Lava Storm deals 2 damage to each attacking creature",
                        DealDamageEffect.toEach(
                                Subjects.controlledBy(Subjects.eachPlayer(),
                                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(), new PermanentIsAttackingPredicate()))), 2)),
                new ChooseOneEffect.ChooseOneOption("Lava Storm deals 2 damage to each blocking creature",
                        DealDamageEffect.toEach(
                                Subjects.controlledBy(Subjects.eachPlayer(),
                                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(), new PermanentIsBlockingPredicate()))), 2))
        )));
    }
}
