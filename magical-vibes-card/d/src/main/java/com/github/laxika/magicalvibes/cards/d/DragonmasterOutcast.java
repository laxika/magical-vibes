package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WWK", collectorNumber = "81")
@CardRegistration(set = "BFZ", collectorNumber = "144")
@CardRegistration(set = "HA2", collectorNumber = "11")
@CardRegistration(set = "TDC", collectorNumber = "211")
@CardRegistration(set = "AFC", collectorNumber = "124")
@CardRegistration(set = "C19", collectorNumber = "139")
@CardRegistration(set = "ONC", collectorNumber = "98")
@CardRegistration(set = "SCD", collectorNumber = "136")
@CardRegistration(set = "FDC", collectorNumber = "153")
public class DragonmasterOutcast extends Card {

    public DragonmasterOutcast() {
        // At the beginning of your upkeep, if you control six or more lands, create a 5/5 red Dragon creature token with flying.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new ControlsPermanentCount(6, new PermanentIsLandPredicate()),
                new CreateTokenEffect("Dragon", 5, 5, CardColor.RED, List.of(CardSubtype.DRAGON),
                        Set.of(Keyword.FLYING), Set.of())));
    }
}
