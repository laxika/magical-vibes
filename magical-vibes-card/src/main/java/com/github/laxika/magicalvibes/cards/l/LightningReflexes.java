package com.github.laxika.magicalvibes.cards.l;

import java.util.Set;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.FlashCastWithCleanupSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MIR", collectorNumber = "186")
public class LightningReflexes extends Card {

    public LightningReflexes() {
        // Enchant creature; the enchanted creature gets +1/+0 and has first strike. The Mirage flash
        // clause lets it be cast at instant speed at the cost of being sacrificed at the next cleanup
        // step when cast at a time a sorcery couldn't have been cast.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new FlashCastWithCleanupSacrificeEffect())
                .addEffect(EffectSlot.STATIC,
                        new StaticBoostEffect(1, 0, Set.of(Keyword.FIRST_STRIKE), GrantScope.ENCHANTED_CREATURE));
    }
}
