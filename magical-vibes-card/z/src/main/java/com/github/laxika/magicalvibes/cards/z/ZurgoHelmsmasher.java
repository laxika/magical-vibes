package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KTK", collectorNumber = "214")
@CardRegistration(set = "DDN", collectorNumber = "1")
@CardRegistration(set = "SLD", collectorNumber = "1028")
public class ZurgoHelmsmasher extends Card {

    public ZurgoHelmsmasher() {
        // Zurgo attacks each combat if able.
        addStaticEffect(new MustAttackEffect());

        // During your turn, Zurgo has indestructible.
        addStaticEffect(new ConditionalEffect(new ControllerTurn(),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)));

        // Whenever a creature dealt damage by Zurgo this turn dies, put a +1/+1 counter on Zurgo.
        addTrigger(Triggers.creatureDies(ObjectRelation.DAMAGED_BY_THIS), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
