package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;

/**
 * Elusive Krasis — 0/4 with evolve that can't be blocked. Evolve is auto-loaded as
 * {@code Keyword.EVOLVE} and handled by the ally-creature entry scan, so only the
 * unblockable static needs wiring here.
 */
@CardRegistration(set = "GTC", collectorNumber = "160")
public class ElusiveKrasis extends Card {

    public ElusiveKrasis() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
    }
}
