package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "40K", collectorNumber = "32")
public class Chronomancer extends Card {

    public Chronomancer() {
        // Atomic Transmutation — {1}, {T}, Sacrifice another artifact: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "another artifact"),
                        new DrawCardEffect(1)
                ),
                "{1}, {T}, Sacrifice another artifact: Draw a card."
        ));

        // Unearth {2}{B}: Return this card from your graveyard to the battlefield. It gains haste.
        // Exile it at the beginning of the next end step or if it would leave the battlefield.
        // Unearth only as a sorcery.
        addUnearth("{2}{B}");
    }
}
