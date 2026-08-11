package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KTK", collectorNumber = "46")
public class MistfireWeaver extends Card {

    public MistfireWeaver() {
        addMorph("{2}{U}");
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_TURNED_FACE_UP,
                        new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.TARGET));
    }
}
