package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "181")
public class SunbathingRootwalla extends Card {

    public SunbathingRootwalla() {
        // Domain — {3}{G}: This creature gets +1/+1 for each basic land type among lands you control.
        addActivatedAbility(new ActivatedAbility(false, "{3}{G}",
                List.of(new BoostSelfEffect(new BasicLandTypesAmongControlledLands(),
                        new BasicLandTypesAmongControlledLands())),
                "Domain — {3}{G}: This creature gets +1/+1 for each basic land type among lands you control. "
                        + "Activate only once each turn.",
                1));
    }
}
