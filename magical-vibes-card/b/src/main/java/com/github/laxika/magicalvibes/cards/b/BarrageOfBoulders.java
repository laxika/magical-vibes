package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

@CardRegistration(set = "KTK", collectorNumber = "100")
public class BarrageOfBoulders extends Card {

    public BarrageOfBoulders() {
        addSpellEffect(DealDamageEffect.toEachCreature(1,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        addSpellEffect(new ConditionalEffect(
                new ControlsPermanent(new PermanentAllOfPredicate(java.util.List.of(
                        new PermanentIsCreaturePredicate(), new PermanentPowerAtLeastPredicate(4)))),
                new CantBlockThisTurnEffect(Subjects.controlledBy(Subjects.eachPlayer()))));
    }
}
