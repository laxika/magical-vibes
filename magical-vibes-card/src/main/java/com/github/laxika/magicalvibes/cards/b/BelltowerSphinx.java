package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerMillsEffect;

/**
 * Belltower Sphinx — 4U 2/5 Sphinx with flying and
 * "Whenever a source deals damage to this creature, that source's controller mills that many cards."
 */
@CardRegistration(set = "M12", collectorNumber = "46")
@CardRegistration(set = "RAV", collectorNumber = "38")
public class BelltowerSphinx extends Card {

    public BelltowerSphinx() {
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new DamageSourceControllerMillsEffect());
    }
}
