package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "87")
public class LightningCloud extends Card {

    public LightningCloud() {
        // Whenever a player casts a red spell, you may pay {R}. If you do, this enchantment deals 1
        // damage to any target.
        // Target chosen as the trigger goes on the stack; the collector wraps the damage in
        // MayPayManaEffect so {R} is paid at resolution (Malachite Talisman shape).
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardColorPredicate(CardColor.RED),
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{R}"
        ));
    }
}
