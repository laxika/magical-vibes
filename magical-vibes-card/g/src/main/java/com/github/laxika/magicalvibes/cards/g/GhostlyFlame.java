package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageSourcesOfColorsAreColorlessEffect;

import java.util.Set;

@CardRegistration(set = "ICE", collectorNumber = "292")
public class GhostlyFlame extends Card {

    public GhostlyFlame() {
        // Black and/or red permanents and spells are colorless sources of damage. This applies to
        // damage only: those sources keep their colours for blocking, targeting, and enchanting.
        addEffect(EffectSlot.STATIC, new DamageSourcesOfColorsAreColorlessEffect(
                Set.of(CardColor.BLACK, CardColor.RED)));
    }
}
