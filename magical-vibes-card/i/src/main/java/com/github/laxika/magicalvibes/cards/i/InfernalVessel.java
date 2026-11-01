package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToOwnerBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "63")
public class InfernalVessel extends Card {

    public InfernalVessel() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new TriggeringPermanentConditionalEffect(
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.DEMON)),
                new ReturnDyingCreatureToOwnerBattlefieldEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 2, CardSubtype.DEMON, Set.of())));
    }
}
