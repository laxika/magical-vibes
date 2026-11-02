package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddManaOfTypeProducedByTappedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToTargetPlayerUntilPlaneswalkEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerCantCastSpellsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OHOP", collectorNumber = "7")
@CardRegistration(set = "OPCA", collectorNumber = "21")
public class ElorenWilds extends Card {

    public ElorenWilds() {
        addTrigger(Triggers.playerTapsPermanentForMana(),
                new AddManaOfTypeProducedByTappedPermanentEffect());
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new GrantStaticEffectToTargetPlayerUntilPlaneswalkEffect(
                        new PlayerCantCastSpellsEffect()));
    }
}
