package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetSpellOrCreatureOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WOE", collectorNumber = "61")
public class MisleadingMotes extends Card {

    public MisleadingMotes() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new PutTargetSpellOrCreatureOnTopOrBottomOfLibraryEffect());
    }
}
