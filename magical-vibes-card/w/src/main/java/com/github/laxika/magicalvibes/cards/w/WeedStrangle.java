package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TargetToughness;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LRW", collectorNumber = "147")
public class WeedStrangle extends Card {

    public WeedStrangle() {
        // Destroy target creature. Clash with an opponent. If you win, you gain life equal to that
        // creature's toughness.
        //
        // The instructions are followed in the order written (CR 608.2c), so the destroy happens
        // before the clash. On a clash win the life gain uses the creature's last-known toughness.
        target(TargetFilters.creature())
                .addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(new ClashEffect(new GainLifeEffect(new TargetToughness())));
    }
}
