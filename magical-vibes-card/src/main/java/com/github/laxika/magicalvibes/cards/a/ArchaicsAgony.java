package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsEqualToStackEntryExcessDamageMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "SOS", collectorNumber = "107")
public class ArchaicsAgony extends Card {

    public ArchaicsAgony() {
        // Converge — Archaic's Agony deals X damage to target creature, where X is the number of
        // colors of mana spent to cast this spell. Exile cards from the top of your library equal
        // to the excess damage dealt to that creature this way. You may play those cards until the
        // end of your next turn.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                .addEffect(EffectSlot.SPELL, new DealXDamageToTargetCreatureEffect())
                .addEffect(EffectSlot.SPELL, new ExileTopCardsEqualToStackEntryExcessDamageMayPlayUntilNextTurnEffect());
    }
}
