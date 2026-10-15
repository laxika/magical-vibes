package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockEachCombatEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToSelfAndOpponentGainsControlEffect;

@CardRegistration(set = "40K", collectorNumber = "79")
public class KhrnTheBetrayer extends Card {

    public KhrnTheBetrayer() {
        // Berzerker — attacks or blocks each combat if able.
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
        addEffect(EffectSlot.STATIC, new MustBlockEachCombatEffect());

        // Sigil of Corruption — draw two cards when you lose control of Khârn.
        addEffect(EffectSlot.ON_SELF_LOSES_CONTROL, new DrawCardEffect(2));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new DrawCardEffect(2));

        // The Betrayer — prevent damage to Khârn and immediately give it to an opponent.
        addEffect(EffectSlot.STATIC, new PreventDamageToSelfAndOpponentGainsControlEffect());
    }
}
