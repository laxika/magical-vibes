package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "M15", collectorNumber = "242")
@CardRegistration(set = "DST", collectorNumber = "164")
@CardRegistration(set = "DDF", collectorNumber = "72")
@CardRegistration(set = "MM2", collectorNumber = "238")
@CardRegistration(set = "DDU", collectorNumber = "65")
@CardRegistration(set = "SLD", collectorNumber = "608")
@CardRegistration(set = "2XM", collectorNumber = "315")
@CardRegistration(set = "EA1", collectorNumber = "20")
@CardRegistration(set = "C14", collectorNumber = "290")
@CardRegistration(set = "MB2", collectorNumber = "107")
@CardRegistration(set = "C21", collectorNumber = "285")
@CardRegistration(set = "BRC", collectorNumber = "180")
@CardRegistration(set = "C18", collectorNumber = "241")
@CardRegistration(set = "C16", collectorNumber = "288")
@CardRegistration(set = "FDC", collectorNumber = "300")
public class DarksteelCitadel extends Card {

    public DarksteelCitadel() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
