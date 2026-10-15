package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "CON", collectorNumber = "52")
public class SalvageSlasher extends Card {

    public SalvageSlasher() {
        // Salvage Slasher gets +1/+0 for each artifact card in your graveyard.
        CardsInGraveyard artifactCardsInGraveyard =
                new CardsInGraveyard(new CardTypePredicate(CardType.ARTIFACT), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(artifactCardsInGraveyard, new Fixed(0)));
    }
}
