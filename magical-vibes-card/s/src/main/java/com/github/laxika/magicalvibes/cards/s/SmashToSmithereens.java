package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SHM", collectorNumber = "107")
@CardRegistration(set = "ORI", collectorNumber = "163")
@CardRegistration(set = "MM2", collectorNumber = "124")
@CardRegistration(set = "MB1", collectorNumber = "124")
@CardRegistration(set = "PIO", collectorNumber = "343")
public class SmashToSmithereens extends Card {

    public SmashToSmithereens() {
        // Destroy target artifact. Smash to Smithereens deals 3 damage to that artifact's controller.
        // The rider stays controlled by the caster (damage source and shields key on the spell) while
        // retargeting the damaged player to the destroyed artifact's controller.
        target(TargetFilters.artifact()).addSpellEffect(new DestroyTargetPermanentThenEffect(
                new DealDamageToPlayersEffect(3, DamageRecipient.TARGET_PLAYER),
                ThenEffectRecipient.TARGET_CONTROLLER_AS_TARGET));
    }
}
