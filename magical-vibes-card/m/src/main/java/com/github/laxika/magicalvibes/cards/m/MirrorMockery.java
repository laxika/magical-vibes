package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DTK", collectorNumber = "62")
public class MirrorMockery extends Card {

    public MirrorMockery() {
        target(TargetFilters.creature());
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                CreateTokenCopyOfEnchantedPermanentEffect.exiledAtEndOfCombat(),
                "Create a token that's a copy of that creature?"));
    }
}
