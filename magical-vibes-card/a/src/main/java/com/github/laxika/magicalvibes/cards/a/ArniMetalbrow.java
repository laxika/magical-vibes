package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentManaValue;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MAT", collectorNumber = "16")
@CardRegistration(set = "MAT", collectorNumber = "66")
@CardRegistration(set = "MAT", collectorNumber = "116")
@CardRegistration(set = "MAT", collectorNumber = "159")
@CardRegistration(set = "MAT", collectorNumber = "200")
public class ArniMetalbrow extends Card {

    public ArniMetalbrow() {
        MayPayManaEffect ability = new MayPayManaEffect(
                "{1}{R}",
                PutCardToBattlefieldEffect.tappedAndAttacking(new CardTypePredicate(CardType.CREATURE), "creature")
                        .boundedBy(new Sum(new TriggeringPermanentManaValue(), new Fixed(-1))),
                "Put a creature card from your hand onto the battlefield tapped and attacking?"
        );
        addTrigger(Triggers.creatureYouControlAttacks(), ability);
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(Triggers.CREATURE, new PermanentIsAttackingPredicate())),
                        ObjectRelation.YOU_CONTROL),
                ability);
    }
}
