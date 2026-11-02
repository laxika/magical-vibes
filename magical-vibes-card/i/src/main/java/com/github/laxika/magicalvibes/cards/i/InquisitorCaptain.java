package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.CardsInHandGraveyardAndLibraryMatchingAtLeast;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SeekTwoCardsToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "8")
public class InquisitorCaptain extends Card {

    public InquisitorCaptain() {
        CardAllOfPredicate eligibleCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardMaxManaValuePredicate(3)));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(
                new AllOf(List.of(
                        new WasCast(),
                        new CardsInHandGraveyardAndLibraryMatchingAtLeast(20, eligibleCreature))),
                new SeekTwoCardsToBattlefieldEffect(eligibleCreature)));
    }
}
