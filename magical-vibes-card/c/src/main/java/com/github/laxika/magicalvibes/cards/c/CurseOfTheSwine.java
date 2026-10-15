package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THS", collectorNumber = "46")
@CardRegistration(set = "SOC", collectorNumber = "192")
@CardRegistration(set = "C21", collectorNumber = "118")
@CardRegistration(set = "TDC", collectorNumber = "149")
@CardRegistration(set = "M3C", collectorNumber = "179")
@CardRegistration(set = "OTC", collectorNumber = "92")
@CardRegistration(set = "LCC", collectorNumber = "150")
@CardRegistration(set = "KHC", collectorNumber = "37")
public class CurseOfTheSwine extends Card {

    public CurseOfTheSwine() {
        targetExactlyX(TargetFilters.creature(), 100).addEffect(EffectSlot.SPELL,
                new ExileTargetPermanentEffect(new CreateTokenEffect(
                        "Boar", 2, 2, CardColor.GREEN, List.of(CardSubtype.BOAR), Set.of(), Set.of())));
    }
}
