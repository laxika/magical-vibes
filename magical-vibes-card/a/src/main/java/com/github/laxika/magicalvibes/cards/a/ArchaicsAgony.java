package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOS", collectorNumber = "107")
public class ArchaicsAgony extends Card {

    public ArchaicsAgony() {
        // Converge — Archaic's Agony deals X damage to target creature, where X is the number of
        // colors of mana spent to cast this spell. Exile cards from the top of your library equal
        // to the excess damage dealt to that creature this way. You may play those cards until the
        // end of your next turn.
        target(TargetFilters.creature())
                .addSpellEffect(new DealDamageToTargetCreatureEffect(new XValue()))
                // Exile top cards equal to the excess damage dealt above (stored on the entry's
                // event value by the damage handler) — playable until the end of your next turn.
                .addSpellEffect(new ExileTopCardsMayPlayUntilNextTurnEffect(new EventValue()));
    }
}
