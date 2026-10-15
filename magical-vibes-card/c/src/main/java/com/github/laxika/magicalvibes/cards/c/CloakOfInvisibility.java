package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MIR", collectorNumber = "58")
public class CloakOfInvisibility extends Card {

    public CloakOfInvisibility() {
        target(TargetFilters.creature())
                // Enchanted creature has phasing...
                .addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                        Keyword.PHASING, GrantScope.ENCHANTED_CREATURE))
                // ...and can't be blocked except by Walls.
                .addEffect(EffectSlot.STATIC, new CanBeBlockedOnlyByFilterEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.WALL),
                        "Walls"));
    }
}
