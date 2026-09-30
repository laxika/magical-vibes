package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "M12", collectorNumber = "217")
@CardRegistration(set = "MRD", collectorNumber = "245")
@CardRegistration(set = "FDN", collectorNumber = "257")
@CardRegistration(set = "M21", collectorNumber = "239")
@CardRegistration(set = "MPS", collectorNumber = "25")
@CardRegistration(set = "DDU", collectorNumber = "62")
@CardRegistration(set = "SLD", collectorNumber = "791")
@CardRegistration(set = "SLD", collectorNumber = "1113")
@CardRegistration(set = "SLD", collectorNumber = "1725")
@CardRegistration(set = "TSR", collectorNumber = "400")
@CardRegistration(set = "SOC", collectorNumber = "355")
@CardRegistration(set = "MSC", collectorNumber = "215")
@CardRegistration(set = "MSC", collectorNumber = "453")
@CardRegistration(set = "CMD", collectorNumber = "262")
@CardRegistration(set = "C14", collectorNumber = "271")
@CardRegistration(set = "C15", collectorNumber = "269")
@CardRegistration(set = "WHO", collectorNumber = "246")
@CardRegistration(set = "WHO", collectorNumber = "837")
@CardRegistration(set = "WHO", collectorNumber = "480")
@CardRegistration(set = "WHO", collectorNumber = "1071")
@CardRegistration(set = "PIP", collectorNumber = "240")
@CardRegistration(set = "PIP", collectorNumber = "486")
@CardRegistration(set = "PIP", collectorNumber = "768")
@CardRegistration(set = "PIP", collectorNumber = "1014")
@CardRegistration(set = "C21", collectorNumber = "264")
@CardRegistration(set = "C20", collectorNumber = "253")
@CardRegistration(set = "DSC", collectorNumber = "253")
@CardRegistration(set = "TDC", collectorNumber = "325")
@CardRegistration(set = "MKC", collectorNumber = "238")
@CardRegistration(set = "AFC", collectorNumber = "216")
@CardRegistration(set = "DMC", collectorNumber = "191")
public class SolemnSimulacrum extends Card {

    public SolemnSimulacrum() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new SearchLibraryEffect(CardPredicateUtils.basicLand(),
                        LibrarySearchDestination.BATTLEFIELD_TAPPED),
                        "Search your library for a basic land card?"));
        addEffect(EffectSlot.ON_DEATH, new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
