package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreatureSubtypeDeathsThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CHK", collectorNumber = "206")
public class DrippingTongueZubera extends Card {

    public DrippingTongueZubera() {
        // When this creature dies, create a 1/1 colorless Spirit creature token for each Zubera that died this turn.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new CreateTokenEffect(
                new CreatureSubtypeDeathsThisTurn(CardSubtype.ZUBERA, CountScope.ANY_PLAYER),
                "Spirit", 1, 1, null, List.of(CardSubtype.SPIRIT), Set.of(), Set.of()));
    }
}
