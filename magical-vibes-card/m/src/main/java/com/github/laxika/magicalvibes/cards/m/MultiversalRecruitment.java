package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "629")
public class MultiversalRecruitment extends Card {

    public MultiversalRecruitment() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, CreateTokenCopyOfTargetPermanentEffect.nonLegendary(
                        java.util.List.of(), Set.of(), null, null, Map.of()));
        addCastingOption(new FlashbackCast("{5}{U}{U}"));
    }
}
