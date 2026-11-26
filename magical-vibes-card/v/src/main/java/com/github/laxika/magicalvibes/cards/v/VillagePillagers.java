package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ECC", collectorNumber = "14")
@CardRegistration(set = "ECC", collectorNumber = "34")
public class VillagePillagers extends Card {

    public VillagePillagers() {
        PermanentPredicate opponentsCreatures = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                DealDamageEffect.toEach(Subjects.controlledBy(Subjects.eachPlayer(), opponentsCreatures), 1));
        addTrigger(Triggers.creatureDies(new PermanentHasCountersPredicate(CounterType.ANY),
                        ObjectRelation.OPPONENT_CONTROLS),
                CreateTokenEffect.ofTreasureToken(1, true));
    }
}
