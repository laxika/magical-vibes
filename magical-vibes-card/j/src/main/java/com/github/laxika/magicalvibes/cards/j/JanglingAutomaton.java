package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;

@CardRegistration(set = "WTH", collectorNumber = "151")
public class JanglingAutomaton extends Card {

    public JanglingAutomaton() {
        // Whenever this creature attacks, untap all creatures defending player controls.
        addEffect(EffectSlot.ON_ATTACK, new UntapPermanentsEffect(
                TapUntapScope.ALL_CREATURES,
                new PermanentControlledByDefendingPlayerPredicate()));
    }
}
