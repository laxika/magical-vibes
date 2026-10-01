package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.n.NestingInstinct;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastSourceCardFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;

@CardRegistration(set = "YTDM", collectorNumber = "5")
public class PearlLakeWarden extends Card {

    public PearlLakeWarden() {
        setBackFaceCard(new NestingInstinct());
        addCastingOption(new AdventureCast("{2}{G}"));
        addEffect(EffectSlot.STATIC, new LookAtTopCardOfOwnLibraryEffect());
        addEffect(EffectSlot.STATIC, new AllowCastSourceCardFromTopOfLibraryEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "NestingInstinct";
    }
}
