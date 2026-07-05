package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsChooseOneToHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "SOS", collectorNumber = "235")
public class StressDream extends Card {

    public StressDream() {
        // Stress Dream deals 5 damage to up to one target creature.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ), 0, 1).addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(5));

        // Look at the top two cards of your library. Put one of those cards into your hand and
        // the other on the bottom of your library.
        addEffect(EffectSlot.SPELL, new LookAtTopCardsChooseOneToHandRestOnBottomEffect(2));
    }
}
