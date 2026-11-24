package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtDamageToSourceThisTurnPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LEG", collectorNumber = "49")
public class BrineHag extends Card {

    public BrineHag() {
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new SetBasePowerToughnessEffect(0, 2, Subjects.allCreatures(), Duration.PERMANENT,
                        new PermanentDealtDamageToSourceThisTurnPredicate()));
    }
}
