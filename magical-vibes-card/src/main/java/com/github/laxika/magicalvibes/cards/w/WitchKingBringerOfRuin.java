package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasLeastPowerAmongControllerCreaturesPredicate;

@CardRegistration(set = "HOC", collectorNumber = "21")
@CardRegistration(set = "HOC", collectorNumber = "61")
public class WitchKingBringerOfRuin extends Card {

    public WitchKingBringerOfRuin() {
        // Whenever Witch-king attacks, defending player sacrifices a creature with the least power
        // among creatures they control.
        addEffect(EffectSlot.ON_ATTACK, new SacrificePermanentsEffect(
                1, new PermanentHasLeastPowerAmongControllerCreaturesPredicate(),
                SacrificeRecipient.DEFENDING_PLAYER));
    }
}
