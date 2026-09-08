package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "RNA", collectorNumber = "13")
public class JusticiarsPortal extends Card {

    public JusticiarsPortal() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL,
                        FlickerEffect.flickerTargetWithKeywords(Set.of(Keyword.FIRST_STRIKE)));
    }
}
