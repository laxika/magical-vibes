package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "6")
public class Aurification extends Card {

    public Aurification() {
        var goldCounterBearer = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasCountersPredicate(CounterType.GOLD)));

        addTrigger(Triggers.permanentDealsDamageToYou().withFilter(new PermanentIsCreaturePredicate()),
                new PutCountersEffect(Subjects.triggering(), CounterType.GOLD));
        addStaticEffect(new GrantSubtypeEffect(CardSubtype.WALL, Subjects.allCreatures(), false,
                        goldCounterBearer));
        addStaticEffect(new GrantKeywordEffect(Keyword.DEFENDER, Subjects.allCreatures(),
                        goldCounterBearer));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                RemoveCountersEffect.allFrom(Subjects.allCreatures(), CounterType.GOLD));
    }
}
