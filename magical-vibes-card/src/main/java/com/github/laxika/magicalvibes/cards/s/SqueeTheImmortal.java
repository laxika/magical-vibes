package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExileCast;
import com.github.laxika.magicalvibes.model.GraveyardCast;

@CardRegistration(set = "DOM", collectorNumber = "146")
public class SqueeTheImmortal extends Card {

    public SqueeTheImmortal() {
        // You may cast this card from your graveyard or from exile.
        addCastingOption(new GraveyardCast());
        addCastingOption(new ExileCast());
    }
}
