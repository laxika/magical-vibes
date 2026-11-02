package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "122")
@CardRegistration(set = "DBL", collectorNumber = "122")
public class SlaughterSpecialist extends Card {

    public SlaughterSpecialist() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new EachOpponentCreatesTokenEffect(
                new CreateTokenEffect("Human", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.HUMAN), Set.of(), Set.of())));
        addTrigger(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
