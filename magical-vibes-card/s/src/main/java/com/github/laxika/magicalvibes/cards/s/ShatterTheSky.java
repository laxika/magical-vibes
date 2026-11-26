package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "37")
@CardRegistration(set = "THB", collectorNumber = "302")
@CardRegistration(set = "ECC", collectorNumber = "66")
public class ShatterTheSky extends Card {

    public ShatterTheSky() {
        PermanentPredicate creaturePowerAtLeastFour = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtLeastPredicate(4)));
        addSpellEffect(DrawCardEffect.eachEvaluatedFor(
                new FixedIfCondition(new ControlsPermanentCount(1, creaturePowerAtLeastFour), 1, 0), Subjects.eachPlayer()));
        addSpellEffect(new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));
    }
}
