package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "ULG", collectorNumber = "32")
@CardRegistration(set = "VMA", collectorNumber = "70")
@CardRegistration(set = "SLD", collectorNumber = "228")
@CardRegistration(set = "SLD", collectorNumber = "1996")
@CardRegistration(set = "UMA", collectorNumber = "57")
@CardRegistration(set = "DMR", collectorNumber = "51")
@CardRegistration(set = "TLE", collectorNumber = "159")
@CardRegistration(set = "CMM", collectorNumber = "96")
@CardRegistration(set = "CMM", collectorNumber = "632")
@CardRegistration(set = "NCC", collectorNumber = "222")
@CardRegistration(set = "TDC", collectorNumber = "153")
@CardRegistration(set = "C20", collectorNumber = "111")
@CardRegistration(set = "WOC", collectorNumber = "93")
public class FranticSearch extends Card {

    public FranticSearch() {
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new DiscardEffect(2, DiscardRecipient.CONTROLLER));
        addSpellEffect(new UntapPermanentsEffect(TapUntapScope.ALL_PERMANENTS, new PermanentIsLandPredicate(), 3));
    }
}
