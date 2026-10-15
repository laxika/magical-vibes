package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "SHM", collectorNumber = "154")
public class ThoughtweftGambit extends Card {

    public ThoughtweftGambit() {
        // Tap all creatures your opponents control (every creature not controlled by you).
        addEffect(EffectSlot.SPELL, new TapPermanentsEffect(
                TapUntapScope.ALL_CREATURES,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        // Untap all creatures you control.
        addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(
                TapUntapScope.CONTROLLED, new PermanentIsCreaturePredicate()));
    }
}
