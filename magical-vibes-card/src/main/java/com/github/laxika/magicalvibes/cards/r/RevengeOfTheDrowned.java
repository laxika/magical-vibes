package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetSpellOrCreatureOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MID", collectorNumber = "72")
public class RevengeOfTheDrowned extends Card {

    public RevengeOfTheDrowned() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new PutTargetSpellOrCreatureOnTopOrBottomOfLibraryEffect())
                .addEffect(EffectSlot.SPELL, CreateTokenEffect.blackZombieWithDecayed(1));
    }
}
