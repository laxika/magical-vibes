package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "INR", collectorNumber = "222")
@CardRegistration(set = "INR", collectorNumber = "423")
@CardRegistration(set = "SOI", collectorNumber = "236")
@CardRegistration(set = "2XM", collectorNumber = "184")
@CardRegistration(set = "SIR", collectorNumber = "223")
@CardRegistration(set = "MKC", collectorNumber = "193")
public class UlvenwaldMysteries extends Card {

    public UlvenwaldMysteries() {
        // Whenever a nontoken creature you control dies, investigate.
        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.YOU_CONTROL), CreateTokenEffect.ofClueToken(1));

        // Whenever you sacrifice a Clue, create a 1/1 white Human Soldier creature token.
        addTrigger(Triggers.sacrificed(new PermanentHasSubtypePredicate(CardSubtype.CLUE), ObjectRelation.YOU_CONTROL),
                new CreateTokenEffect("Human Soldier", 1, 1,
                        CardColor.WHITE, List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER),
                        Set.of(), Set.of()));
    }
}
