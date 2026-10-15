package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutTargetPermanentUntilPlaneswalkEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "579")
public class TheDoctorsChildhoodBarn extends Card {

    public TheDoctorsChildhoodBarn() {
        addEffect(EffectSlot.STATIC,
                new EnterPermanentsOfTypesTappedEffect(Set.of(CardType.CREATURE)));
        target(TargetFilters.nonlandPermanentAnOpponentControls(), 0, 1)
                .addEffect(EffectSlot.CHAOS_TRIGGERED, SequenceEffect.of(
                        new UntapPermanentsEffect(TapUntapScope.TARGET),
                        new PhaseOutTargetPermanentUntilPlaneswalkEffect()));
    }
}
