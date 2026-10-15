package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FIN", collectorNumber = "217")
@CardRegistration(set = "FIN", collectorNumber = "481")
public class CloudOfDarkness extends Card {

    public CloudOfDarkness() {
        CardsInGraveyard permanentCardsInYourGraveyard =
                new CardsInGraveyard(new CardIsPermanentPredicate(), CountScope.CONTROLLER);
        Scaled debuff = new Scaled(permanentCardsInYourGraveyard, -1);
        target(TargetFilters.creatureAnOpponentControls()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new BoostTargetCreatureEffect(debuff, debuff));
    }
}
