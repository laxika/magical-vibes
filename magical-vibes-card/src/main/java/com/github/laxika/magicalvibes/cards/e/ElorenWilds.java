package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddManaOfTypeProducedByTappedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToTargetPlayerUntilPlaneswalkEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerCantCastSpellsEffect;

@CardRegistration(set = "OHOP", collectorNumber = "7")
public class ElorenWilds extends Card {

    public ElorenWilds() {
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_PERMANENT_FOR_MANA,
                new AddManaOfTypeProducedByTappedPermanentEffect());
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new GrantStaticEffectToTargetPlayerUntilPlaneswalkEffect(
                        new PlayerCantCastSpellsEffect()));
    }
}
