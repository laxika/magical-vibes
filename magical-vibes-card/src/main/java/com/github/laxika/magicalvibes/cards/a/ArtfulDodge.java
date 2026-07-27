package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DKA", collectorNumber = "27")
public class ArtfulDodge extends Card {

    public ArtfulDodge() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new MakeCreatureUnblockableEffect());
        addCastingOption(new FlashbackCast("{U}"));
    }
}
