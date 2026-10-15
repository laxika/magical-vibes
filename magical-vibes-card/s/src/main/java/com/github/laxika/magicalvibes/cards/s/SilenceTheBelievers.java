package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetAndAttachedMatchingEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JOU", collectorNumber = "82")
public class SilenceTheBelievers extends Card {

    public SilenceTheBelievers() {
        setAdditionalManaCostPerExtraTarget("{2}{B}");

        target(TargetFilters.creature(), 0, 99)
                .addEffect(EffectSlot.SPELL, new ExileTargetAndAttachedMatchingEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.AURA)));
    }
}
