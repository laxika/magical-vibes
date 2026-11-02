package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToOwnerBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "117")
public class ThunderboltsConspiracy extends Card {

    public ThunderboltsConspiracy() {
        addTrigger(Triggers.creatureDies(
                        new PermanentHasSubtypePredicate(CardSubtype.VILLAIN),
                        ObjectRelation.YOU_CONTROL),
                new ReturnDyingCreatureToOwnerBattlefieldEffect(CounterType.FINALITY, 1, CardSubtype.HERO, Set.of()));
    }
}
