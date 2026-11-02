package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YMKM", collectorNumber = "2")
public class SleuthInstructor extends Card {

    public SleuthInstructor() {
        addMorph("{1}{W}");

        CardAllOfPredicate nonElephantCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardNotPredicate(new CardSubtypePredicate(CardSubtype.ELEPHANT))));
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.YOU_CONTROL),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE),
                SeekLibraryEffect.cloakedToBattlefield(
                        new Fixed(1), nonElephantCreature,
                        new ManaValueBound(new Fixed(3), false, 0)));
    }
}
