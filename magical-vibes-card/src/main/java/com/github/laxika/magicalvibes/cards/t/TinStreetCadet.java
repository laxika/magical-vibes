package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ANB", collectorNumber = "87")
public class TinStreetCadet extends Card {

    public TinStreetCadet() {
        // Whenever this creature becomes blocked, create a 1/1 red Goblin creature token.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new CreateTokenEffect(
                "Goblin", 1, 1, CardColor.RED, List.of(CardSubtype.GOBLIN), Set.of(), Set.of()));
    }
}
