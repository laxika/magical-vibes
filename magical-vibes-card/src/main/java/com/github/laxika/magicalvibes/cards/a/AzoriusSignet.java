package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "159")
@CardRegistration(set = "C18", collectorNumber = "196")
@CardRegistration(set = "MM3", collectorNumber = "215")
@CardRegistration(set = "SLD", collectorNumber = "286")
@CardRegistration(set = "GK2", collectorNumber = "24")
@CardRegistration(set = "AA1", collectorNumber = "1")
@CardRegistration(set = "RVR", collectorNumber = "250")
@CardRegistration(set = "DSC", collectorNumber = "240")
@CardRegistration(set = "TDC", collectorNumber = "312")
@CardRegistration(set = "MKC", collectorNumber = "224")
@CardRegistration(set = "C20", collectorNumber = "238")
@CardRegistration(set = "BLC", collectorNumber = "265")
@CardRegistration(set = "NEC", collectorNumber = "145")
@CardRegistration(set = "VOC", collectorNumber = "161")
@CardRegistration(set = "BRC", collectorNumber = "133")
public class AzoriusSignet extends Card {

    public AzoriusSignet() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardManaEffect(ManaColor.WHITE), new AwardManaEffect(ManaColor.BLUE)),
                "{1}, {T}: Add {W}{U}."
        ));
    }
}
