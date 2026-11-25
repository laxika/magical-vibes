package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "122")
@CardRegistration(set = "DBL", collectorNumber = "122")
public class SlaughterSpecialist extends Card {

    public SlaughterSpecialist() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new EachPlayerCreatesTokenEffect(
                new CreateTokenEffect("Human", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.HUMAN), Set.of(), Set.of()), Subjects.eachOpponent()));
        addTrigger(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
