package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "94")
public class HordeOfBoggarts extends Card {

    // Menace is auto-loaded from Scryfall keywords.
    public HordeOfBoggarts() {
        PermanentCount redPermanentsYouControl =
                new PermanentCount(new PermanentColorInPredicate(Set.of(CardColor.RED)), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(redPermanentsYouControl, redPermanentsYouControl));
    }
}
