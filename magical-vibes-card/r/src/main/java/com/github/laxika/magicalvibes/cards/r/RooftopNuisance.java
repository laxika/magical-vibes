package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CasualtyCost;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellIfCasualtyPaidEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SNC", collectorNumber = "57")
public class RooftopNuisance extends Card {

    public RooftopNuisance() {
        addEffect(EffectSlot.ON_SELF_CAST, new CopyThisSpellIfCasualtyPaidEffect());
        addCastingCost(new CasualtyCost(1));
        target(TargetFilters.creature())
                .addSpellEffect(new TapPermanentsEffect(TapUntapScope.TARGET))
                .addSpellEffect(new SkipNextUntapEffect(TapUntapScope.TARGET));
        addSpellEffect(new DrawCardEffect(1));
    }
}
