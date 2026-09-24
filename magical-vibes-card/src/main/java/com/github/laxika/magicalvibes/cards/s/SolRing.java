package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "SUM", collectorNumber = "274")
@CardRegistration(set = "2ED", collectorNumber = "270")
@CardRegistration(set = "3ED", collectorNumber = "274")
@CardRegistration(set = "V10", collectorNumber = "12")
@CardRegistration(set = "VMA", collectorNumber = "283")
@CardRegistration(set = "MPS", collectorNumber = "24")
@CardRegistration(set = "SLD", collectorNumber = "249")
@CardRegistration(set = "SLD", collectorNumber = "910")
@CardRegistration(set = "SLD", collectorNumber = "912")
@CardRegistration(set = "SLD", collectorNumber = "913")
@CardRegistration(set = "SLD", collectorNumber = "1011")
@CardRegistration(set = "SLD", collectorNumber = "1074")
@CardRegistration(set = "SLD", collectorNumber = "1494")
@CardRegistration(set = "SLD", collectorNumber = "1512")
@CardRegistration(set = "SLD", collectorNumber = "1604")
@CardRegistration(set = "SLD", collectorNumber = "1664")
@CardRegistration(set = "SLD", collectorNumber = "1696")
@CardRegistration(set = "SLD", collectorNumber = "1734")
@CardRegistration(set = "SLD", collectorNumber = "1833")
@CardRegistration(set = "SLD", collectorNumber = "1905")
@CardRegistration(set = "SLD", collectorNumber = "1988")
@CardRegistration(set = "SLD", collectorNumber = "1993")
@CardRegistration(set = "SLD", collectorNumber = "2063")
@CardRegistration(set = "SLD", collectorNumber = "2093")
@CardRegistration(set = "MB1", collectorNumber = "222")
@CardRegistration(set = "SLC", collectorNumber = "19")
@CardRegistration(set = "SLC", collectorNumber = "46")
@CardRegistration(set = "TLE", collectorNumber = "316")
@CardRegistration(set = "C13", collectorNumber = "259")
@CardRegistration(set = "C14", collectorNumber = "270")
@CardRegistration(set = "C15", collectorNumber = "268")
@CardRegistration(set = "TMC", collectorNumber = "59")
@CardRegistration(set = "CMD", collectorNumber = "261")
@CardRegistration(set = "SLZ", collectorNumber = "114")
@CardRegistration(set = "SLZ", collectorNumber = "235")
@CardRegistration(set = "SLZ", collectorNumber = "356")
@CardRegistration(set = "ME4", collectorNumber = "227")
@CardRegistration(set = "ECC", collectorNumber = "57")
@CardRegistration(set = "ECC", collectorNumber = "58")
@CardRegistration(set = "CMM", collectorNumber = "410")
@CardRegistration(set = "CMM", collectorNumber = "703")
public class SolRing extends Card {

    public SolRing() {
        // {T}: Add {C}{C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS, 2)),
                "{T}: Add {C}{C}."
        ));
    }
}
