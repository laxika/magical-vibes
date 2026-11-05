package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.CanBeholdSubtype;
import com.github.laxika.magicalvibes.model.effect.BeholdEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "118")
@CardRegistration(set = "FDC", collectorNumber = "172")
public class SarkhanDragonAscendant extends Card {

    public SarkhanDragonAscendant() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConditionalEffect(
                        new CanBeholdSubtype(CardSubtype.DRAGON),
                        new MayEffect(
                                new BeholdEffect(CardSubtype.DRAGON, CreateTokenEffect.ofTreasureToken(1)),
                                "Behold a Dragon?")));
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.DRAGON))),
                        ObjectRelation.YOU_CONTROL),
                SequenceEffect.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new GrantSubtypeUntilEndOfTurnEffect(CardSubtype.DRAGON, GrantScope.SELF),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));
    }
}
