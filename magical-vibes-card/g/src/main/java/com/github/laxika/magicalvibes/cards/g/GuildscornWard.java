package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromMulticoloredEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GTC", collectorNumber = "15")
public class GuildscornWard extends Card {

    public GuildscornWard() {
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC,
                new ProtectionFromMulticoloredEffect(GrantScope.ENCHANTED_CREATURE));
    }
}
