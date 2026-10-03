package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "155")
@CardRegistration(set = "MM3", collectorNumber = "224")
@CardRegistration(set = "SLD", collectorNumber = "294")
@CardRegistration(set = "GK2", collectorNumber = "48")
@CardRegistration(set = "AA1", collectorNumber = "9")
@CardRegistration(set = "RVR", collectorNumber = "263")
@CardRegistration(set = "CMD", collectorNumber = "255")
@CardRegistration(set = "C15", collectorNumber = "262")
@CardRegistration(set = "MOC", collectorNumber = "369")
@CardRegistration(set = "C20", collectorNumber = "247")
@CardRegistration(set = "C21", collectorNumber = "254")
@CardRegistration(set = "DSC", collectorNumber = "249")
@CardRegistration(set = "TDC", collectorNumber = "323")
@CardRegistration(set = "OTC", collectorNumber = "261")
@CardRegistration(set = "LCC", collectorNumber = "310")
@CardRegistration(set = "BRC", collectorNumber = "154")
@CardRegistration(set = "C18", collectorNumber = "213")
@CardRegistration(set = "C16", collectorNumber = "266")
public class OrzhovSignet extends Card {

    public OrzhovSignet() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardManaEffect(ManaColor.WHITE), new AwardManaEffect(ManaColor.BLACK)),
                "{1}, {T}: Add {W}{B}."
        ));
    }
}
