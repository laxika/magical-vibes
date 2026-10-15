package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M14", collectorNumber = "86")
@CardRegistration(set = "ORI", collectorNumber = "85")
public class Blightcaster extends Card {

    public Blightcaster() {
        // Whenever you cast an enchantment spell, you may have target creature get -2/-2 until
        // end of turn. The target is chosen as the trigger goes on the stack; the "may" choice
        // is made at resolution, so the MayEffect wraps the boost rather than the trigger.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardTypePredicate(CardType.ENCHANTMENT),
                List.of(new MayEffect(new BoostTargetCreatureEffect(-2, -2),
                        "Have target creature get -2/-2 until end of turn?")),
                null,
                TargetFilters.creature()
        ));
    }
}
