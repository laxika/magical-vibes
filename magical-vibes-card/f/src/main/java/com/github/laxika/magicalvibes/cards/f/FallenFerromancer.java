package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "82")
public class FallenFerromancer extends Card {

    public FallenFerromancer() {
        // Infect is auto-loaded from Scryfall.
        // {1}{R}, {T}: This creature deals 1 damage to any target.
        addActivatedAbility(new ActivatedAbility(true, "{1}{R}",
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{1}{R}, {T}: Fallen Ferromancer deals 1 damage to any target."));
    }
}
