package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.FlashCastWithCleanupSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "VIS", collectorNumber = "38")
public class MysticVeil extends Card {

    public MysticVeil() {
        // Enchant creature. Mirage flash clause: cast as though it had flash; sacrificed at the
        // next cleanup step if cast when a sorcery couldn't have been cast.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new FlashCastWithCleanupSacrificeEffect())
                // Enchanted creature has shroud.
                .addEffect(EffectSlot.STATIC,
                        new GrantKeywordEffect(Keyword.SHROUD, GrantScope.ENCHANTED_CREATURE));
    }
}
