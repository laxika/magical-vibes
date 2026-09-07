package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "VOW", collectorNumber = "26")
public class NurturingPresence extends Card {

    public NurturingPresence() {
        // Enchanted creature has "Whenever a creature you control enters, this creature gets +1/+1
        // until end of turn."
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                        new BoostSelfEffect(1, 1),
                        GrantScope.ENCHANTED_CREATURE));

        // When this Aura enters, create a 1/1 white Spirit creature token with flying.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.whiteSpirit(1));
    }
}
