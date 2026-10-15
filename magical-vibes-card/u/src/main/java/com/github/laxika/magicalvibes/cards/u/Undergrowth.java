package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "ALL", collectorNumber = "102a")
@CardRegistration(set = "ALL", collectorNumber = "102b")
public class Undergrowth extends Card {

    public Undergrowth() {
        // As an additional cost to cast this spell, you may pay {2}{R}.
        addEffect(EffectSlot.STATIC, new KickerEffect("{2}{R}"));

        // Prevent all combat damage that would be dealt this turn. If this spell's additional
        // cost was paid, this effect doesn't affect combat damage that would be dealt by red creatures.
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(new Kicked(),
                PreventDamageEffect.allCombat(),
                PreventDamageEffect.allCombatExcept(new PermanentColorInPredicate(Set.of(CardColor.RED)))));
    }
}
