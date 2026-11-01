package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH1", collectorNumber = "213")
public class RuinationRioter extends Card {

    public RuinationRioter() {
        CardsInGraveyard landsInGraveyard = new CardsInGraveyard(
                new CardTypePredicate(CardType.LAND), CountScope.CONTROLLER);
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(
                new DealDamageToAnyTargetEffect(landsInGraveyard),
                "Have Ruination Rioter deal damage to any target?"));
    }
}
