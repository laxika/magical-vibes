package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "677")
@CardRegistration(set = "SLD", collectorNumber = "697")
@CardRegistration(set = "SLD", collectorNumber = "710")
@CardRegistration(set = "SLD", collectorNumber = "744")
@CardRegistration(set = "SLD", collectorNumber = "758")
@CardRegistration(set = "SLD", collectorNumber = "792")
@CardRegistration(set = "SLD", collectorNumber = "806")
@CardRegistration(set = "SLD", collectorNumber = "917")
@CardRegistration(set = "SLD", collectorNumber = "1000")
@CardRegistration(set = "SLD", collectorNumber = "1496")
@CardRegistration(set = "SLD", collectorNumber = "1666")
@CardRegistration(set = "SLD", collectorNumber = "1697")
@CardRegistration(set = "SLD", collectorNumber = "1989")
@CardRegistration(set = "SLD", collectorNumber = "1994")
@CardRegistration(set = "ANB", collectorNumber = "118")
@CardRegistration(set = "TMC", collectorNumber = "63")
@CardRegistration(set = "REX", collectorNumber = "26")
@CardRegistration(set = "SOC", collectorNumber = "129")
@CardRegistration(set = "C13", collectorNumber = "281")
@CardRegistration(set = "C15", collectorNumber = "281")
@CardRegistration(set = "CMD", collectorNumber = "269")
@CardRegistration(set = "SLZ", collectorNumber = "118")
@CardRegistration(set = "SLZ", collectorNumber = "239")
@CardRegistration(set = "SLZ", collectorNumber = "360")
@CardRegistration(set = "MSC", collectorNumber = "233")
@CardRegistration(set = "MSC", collectorNumber = "234")
@CardRegistration(set = "MSC", collectorNumber = "235")
@CardRegistration(set = "MSC", collectorNumber = "236")
@CardRegistration(set = "ECC", collectorNumber = "59")
@CardRegistration(set = "ECC", collectorNumber = "60")
@CardRegistration(set = "EOC", collectorNumber = "59")
@CardRegistration(set = "CMM", collectorNumber = "420")
@CardRegistration(set = "CMM", collectorNumber = "659")
@CardRegistration(set = "WHO", collectorNumber = "263")
@CardRegistration(set = "WHO", collectorNumber = "264")
@CardRegistration(set = "WHO", collectorNumber = "265")
@CardRegistration(set = "WHO", collectorNumber = "266")
@CardRegistration(set = "WHO", collectorNumber = "854")
@CardRegistration(set = "WHO", collectorNumber = "855")
@CardRegistration(set = "WHO", collectorNumber = "856")
@CardRegistration(set = "WHO", collectorNumber = "857")
@CardRegistration(set = "PIP", collectorNumber = "259")
@CardRegistration(set = "PIP", collectorNumber = "360")
@CardRegistration(set = "PIP", collectorNumber = "787")
@CardRegistration(set = "PIP", collectorNumber = "888")
@CardRegistration(set = "MB2", collectorNumber = "256")
@CardRegistration(set = "C21", collectorNumber = "284")
@CardRegistration(set = "C20", collectorNumber = "264")
@CardRegistration(set = "C19", collectorNumber = "237")
@CardRegistration(set = "C18", collectorNumber = "240")
@CardRegistration(set = "40K", collectorNumber = "270")
@CardRegistration(set = "40K", collectorNumber = "271")
@CardRegistration(set = "40K", collectorNumber = "272")
@CardRegistration(set = "DSC", collectorNumber = "96")
@CardRegistration(set = "LTC", collectorNumber = "301")
@CardRegistration(set = "TDC", collectorNumber = "107")
@CardRegistration(set = "M3C", collectorNumber = "331")
@CardRegistration(set = "MKC", collectorNumber = "256")
@CardRegistration(set = "AFC", collectorNumber = "230")
@CardRegistration(set = "OTC", collectorNumber = "280")
@CardRegistration(set = "LCC", collectorNumber = "325")
@CardRegistration(set = "BLC", collectorNumber = "130")
@CardRegistration(set = "MIC", collectorNumber = "170")
@CardRegistration(set = "NEC", collectorNumber = "167")
@CardRegistration(set = "BRC", collectorNumber = "178")
@CardRegistration(set = "ONC", collectorNumber = "151")
@CardRegistration(set = "WOC", collectorNumber = "156")
@CardRegistration(set = "VOC", collectorNumber = "172")
@CardRegistration(set = "DRC", collectorNumber = "59")
@CardRegistration(set = "DRC", collectorNumber = "60")
public class CommandTower extends Card {

    public CommandTower() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(1, ManaSpendRestriction.COMMANDER_COLOR_IDENTITY)),
                "{T}: Add one mana of any color in your commander's color identity."
        ));
    }
}
