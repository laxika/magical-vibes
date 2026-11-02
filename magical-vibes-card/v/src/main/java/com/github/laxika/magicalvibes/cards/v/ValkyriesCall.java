package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToOwnerBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "27")
@CardRegistration(set = "FDN", collectorNumber = "302")
@CardRegistration(set = "FDN", collectorNumber = "371")
@CardRegistration(set = "FDN", collectorNumber = "449")
public class ValkyriesCall extends Card {

    public ValkyriesCall() {
        addTrigger(Triggers.creatureDies(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.ANGEL)))),
                        ObjectRelation.YOU_CONTROL),
                new ReturnDyingCreatureToOwnerBattlefieldEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, CardSubtype.ANGEL, Set.of(Keyword.FLYING)));
    }
}
