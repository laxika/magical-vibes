package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscoverEffect;

@CardRegistration(set = "LCI", collectorNumber = "153")
@CardRegistration(set = "LCI", collectorNumber = "375")
@CardRegistration(set = "LCI", collectorNumber = "404")
@CardRegistration(set = "FDC", collectorNumber = "161")
public class HitTheMotherLode extends Card {

    public HitTheMotherLode() {
        addSpellEffect(new DiscoverEffect(10));
        addSpellEffect(CreateTokenEffect.ofTreasureToken(
                new Max(new Fixed(0), new Sum(new Fixed(10), new Scaled(new EventValue(), -1))))
                .withTapped(true));
    }
}
