package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ELD", collectorNumber = "87")
public class FestiveFuneral extends Card {

    public FestiveFuneral() {
        Scaled minusCardsInGraveyard = new Scaled(
                new CardsInGraveyard(null, CountScope.CONTROLLER), -1);
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(minusCardsInGraveyard, minusCardsInGraveyard));
    }
}
