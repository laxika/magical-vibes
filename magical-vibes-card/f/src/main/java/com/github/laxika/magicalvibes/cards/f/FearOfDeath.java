package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "59")
@CardRegistration(set = "DBL", collectorNumber = "326")
public class FearOfDeath extends Card {

    public FearOfDeath() {
        CardsInGraveyard controllerGraveyard = new CardsInGraveyard(null, CountScope.CONTROLLER);
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new MillEffect(2, MillRecipient.CONTROLLER))
                .addStaticEffect(new AttachedBoostEffect(
                        new Scaled(controllerGraveyard, -1),
                        new Fixed(0),
                        GrantScope.ENCHANTED_CREATURE));
    }
}
