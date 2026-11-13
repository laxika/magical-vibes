package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ExertAsItAttacksEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "728")
@CardRegistration(set = "SLX", collectorNumber = "28")
public class Themberchaud extends Card {

    public Themberchaud() {
        PermanentAllOfPredicate otherNonflyingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        PermanentCount mountains = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN), CountScope.CONTROLLER);

        // When Themberchaud enters, he deals damage to each other creature without flying and each player.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MassDamageEffect(mountains, true, false, otherNonflyingCreature));

        addStaticEffect(new ExertAsItAttacksEffect());
        addTrigger(Triggers.exertedAsItAttacks(), new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF));
    }
}
