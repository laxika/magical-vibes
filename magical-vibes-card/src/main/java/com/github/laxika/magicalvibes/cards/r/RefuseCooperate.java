package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.Cooperate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

/**
 * Refuse // Cooperate — front half (Refuse).
 * Instant — Refuse deals damage to target spell's controller equal to that spell's mana value.
 * Back half (Cooperate) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "HOU", collectorNumber = "156")
public class RefuseCooperate extends Card {

    public RefuseCooperate() {
        Cooperate cooperate = new Cooperate();
        cooperate.setSetCode(getSetCode());
        cooperate.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(cooperate);

        // Refuse deals damage to target spell's controller equal to that spell's mana value.
        // SPELL_ON_STACK targetSpec on the effect auto-derives "any spell on the stack".
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(
                new TargetSpellManaValue(), DamageRecipient.TARGET_SPELL_CONTROLLER));
    }

    @Override
    public String getBackFaceClassName() {
        return "Cooperate";
    }
}
