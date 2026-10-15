package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "189")
public class GranPulseOchu extends Card {

    public GranPulseOchu() {
        CardsInGraveyard permanentCardsInYourGraveyard =
                new CardsInGraveyard(new CardIsPermanentPredicate(), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{8}",
                List.of(new BoostSelfEffect(permanentCardsInYourGraveyard, permanentCardsInYourGraveyard)),
                "{8}: Until end of turn, this creature gets +1/+1 for each permanent card in your graveyard."
        ));
    }
}
