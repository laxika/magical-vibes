package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "133")
public class OonasProwler extends Card {

    public OonasProwler() {
        // Flying auto-loaded from Scryfall.
        // Discard a card: This creature gets -2/-0 until end of turn. Any player may activate this ability.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new DiscardCardTypeCost(null, null), new BoostSelfEffect(-2, 0)),
                "Discard a card: Oona's Prowler gets -2/-0 until end of turn. Any player may activate this ability."
        ).withActivatableByAnyPlayer());
    }
}
