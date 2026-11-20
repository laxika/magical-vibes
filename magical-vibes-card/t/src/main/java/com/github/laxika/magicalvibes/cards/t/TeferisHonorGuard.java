package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;

import java.util.List;

/**
 * Teferi's Honor Guard — {2}{W} Creature — Human Knight 2/2.
 * Flanking
 * "{U}{U}: This creature phases out."
 */
@CardRegistration(set = "VIS", collectorNumber = "22")
public class TeferisHonorGuard extends Card {

    public TeferisHonorGuard() {
        // Flanking is auto-loaded from Scryfall and handled by the engine.
        addActivatedAbility(new ActivatedAbility(false, "{U}{U}",
                List.of(new PhaseOutEffect(Subjects.source())),
                "{U}{U}: This creature phases out."));
    }
}
