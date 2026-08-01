package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "25")
public class ZhalfirinCrusader extends Card {

    public ZhalfirinCrusader() {
        // Flanking is auto-loaded from Scryfall and handled by the engine.
        // {1}{W}: The next 1 damage that would be dealt to this creature this turn is dealt to any target instead.
        addActivatedAbility(new ActivatedAbility(false, "{1}{W}",
                List.of(new RedirectNextDamageToAnyTargetEffect(1)),
                "{1}{W}: The next 1 damage that would be dealt to this creature this turn is dealt to any target instead."));
    }
}
