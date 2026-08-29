package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RageOfWinter;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "ELD", collectorNumber = "61")
public class QueenOfIce extends Card {

    public QueenOfIce() {
        setBackFaceCard(new RageOfWinter());
        addCastingOption(new AdventureCast("{1}{U}"));

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE,
                new TapPermanentsEffect(TapUntapScope.TARGET));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE,
                new SkipNextUntapEffect(TapUntapScope.TARGET));
    }

    @Override
    public String getBackFaceClassName() {
        return "RageOfWinter";
    }
}
