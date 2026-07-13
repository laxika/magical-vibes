package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "380")
@CardRegistration(set = "10E", collectorNumber = "381")
@CardRegistration(set = "10E", collectorNumber = "382")
@CardRegistration(set = "10E", collectorNumber = "383")
@CardRegistration(set = "M10", collectorNumber = "246")
@CardRegistration(set = "M10", collectorNumber = "247")
@CardRegistration(set = "M10", collectorNumber = "248")
@CardRegistration(set = "M10", collectorNumber = "249")
@CardRegistration(set = "SOM", collectorNumber = "246")
@CardRegistration(set = "SOM", collectorNumber = "247")
@CardRegistration(set = "SOM", collectorNumber = "248")
@CardRegistration(set = "SOM", collectorNumber = "249")
@CardRegistration(set = "MBS", collectorNumber = "154")
@CardRegistration(set = "MBS", collectorNumber = "155")
@CardRegistration(set = "NPH", collectorNumber = "174")
@CardRegistration(set = "NPH", collectorNumber = "175")
@CardRegistration(set = "M11", collectorNumber = "246")
@CardRegistration(set = "M11", collectorNumber = "247")
@CardRegistration(set = "M11", collectorNumber = "248")
@CardRegistration(set = "M11", collectorNumber = "249")
@CardRegistration(set = "ISD", collectorNumber = "262")
@CardRegistration(set = "ISD", collectorNumber = "263")
@CardRegistration(set = "ISD", collectorNumber = "264")
@CardRegistration(set = "DOM", collectorNumber = "266")
@CardRegistration(set = "DOM", collectorNumber = "267")
@CardRegistration(set = "DOM", collectorNumber = "268")
@CardRegistration(set = "DOM", collectorNumber = "269")
@CardRegistration(set = "XLN", collectorNumber = "276")
@CardRegistration(set = "XLN", collectorNumber = "277")
@CardRegistration(set = "XLN", collectorNumber = "278")
@CardRegistration(set = "XLN", collectorNumber = "279")
@CardRegistration(set = "SOS", collectorNumber = "271")
@CardRegistration(set = "SOS", collectorNumber = "280")
@CardRegistration(set = "SOS", collectorNumber = "281")
@CardRegistration(set = "9ED", collectorNumber = "347")
@CardRegistration(set = "9ED", collectorNumber = "348")
@CardRegistration(set = "9ED", collectorNumber = "349")
@CardRegistration(set = "9ED", collectorNumber = "350")
@CardRegistration(set = "POR", collectorNumber = "212")
@CardRegistration(set = "POR", collectorNumber = "213")
@CardRegistration(set = "POR", collectorNumber = "214")
@CardRegistration(set = "POR", collectorNumber = "215")
@CardRegistration(set = "LRW", collectorNumber = "298")
@CardRegistration(set = "LRW", collectorNumber = "299")
@CardRegistration(set = "LRW", collectorNumber = "300")
@CardRegistration(set = "LRW", collectorNumber = "301")
@CardRegistration(set = "P02", collectorNumber = "163")
@CardRegistration(set = "P02", collectorNumber = "164")
@CardRegistration(set = "P02", collectorNumber = "165")
@CardRegistration(set = "PTK", collectorNumber = "178")
@CardRegistration(set = "PTK", collectorNumber = "179")
@CardRegistration(set = "PTK", collectorNumber = "180")
@CardRegistration(set = "8ED", collectorNumber = "347")
@CardRegistration(set = "8ED", collectorNumber = "348")
@CardRegistration(set = "8ED", collectorNumber = "349")
@CardRegistration(set = "8ED", collectorNumber = "350")
@CardRegistration(set = "SHM", collectorNumber = "298")
@CardRegistration(set = "SHM", collectorNumber = "299")
@CardRegistration(set = "SHM", collectorNumber = "300")
@CardRegistration(set = "SHM", collectorNumber = "301")
public class Forest extends Card {

    public Forest() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.GREEN));
    }
}
