package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasCyclingPredicate;

@CardRegistration(set = "HOU", collectorNumber = "80")
public class VileManifestation extends Card {

    public VileManifestation() {
        // Cycling {2} — auto-loaded from the Scryfall keyword.

        // This creature gets +1/+0 for each card with cycling in your graveyard.
        CardsInGraveyard cyclingCardsInGraveyard =
                new CardsInGraveyard(new CardHasCyclingPredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(cyclingCardsInGraveyard, new Fixed(0)));
    }
}
