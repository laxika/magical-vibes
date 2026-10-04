package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import java.util.List;

@CardRegistration(set = "DRC", collectorNumber = "125")
@CardRegistration(set = "FDC", collectorNumber = "250")
@CardRegistration(set = "SLD", collectorNumber = "203")
@CardRegistration(set = "SLD", collectorNumber = "315")
@CardRegistration(set = "CMM", collectorNumber = "377")
@CardRegistration(set = "CMM", collectorNumber = "655")
@CardRegistration(set = "C14", collectorNumber = "54")
@CardRegistration(set = "ECC", collectorNumber = "139")
@CardRegistration(set = "WHO", collectorNumber = "240")
@CardRegistration(set = "WHO", collectorNumber = "831")
@CardRegistration(set = "FIC", collectorNumber = "339")
@CardRegistration(set = "MOC", collectorNumber = "352")
@CardRegistration(set = "C21", collectorNumber = "239")
@CardRegistration(set = "C20", collectorNumber = "240")
@CardRegistration(set = "C19", collectorNumber = "212")
@CardRegistration(set = "C18", collectorNumber = "200")
@CardRegistration(set = "40K", collectorNumber = "233")
@CardRegistration(set = "40K", collectorNumber = "234")
@CardRegistration(set = "40K", collectorNumber = "235")
@CardRegistration(set = "DSC", collectorNumber = "244")
@CardRegistration(set = "LTC", collectorNumber = "276")
@CardRegistration(set = "AFC", collectorNumber = "203")
@CardRegistration(set = "LCC", collectorNumber = "301")
@CardRegistration(set = "DMC", collectorNumber = "181")
@CardRegistration(set = "MIC", collectorNumber = "159")
@CardRegistration(set = "C16", collectorNumber = "248")
@CardRegistration(set = "KHC", collectorNumber = "99")
@CardRegistration(set = "VOC", collectorNumber = "163")
@CardRegistration(set = "BRC", collectorNumber = "135")
@CardRegistration(set = "ONC", collectorNumber = "128")
@CardRegistration(set = "SCD", collectorNumber = "261")
public class CommandersSphere extends Card {

    public CommandersSphere() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(1, ManaSpendRestriction.COMMANDER_COLOR_IDENTITY)),
                "{T}: Add one mana of any color in your commander's color identity."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "Sacrifice this artifact: Draw a card."
        ));
    }
}
