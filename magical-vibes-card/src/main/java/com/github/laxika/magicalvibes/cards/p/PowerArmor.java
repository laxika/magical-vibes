package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "309")
public class PowerArmor extends Card {

    public PowerArmor() {
        addActivatedAbility(new ActivatedAbility(true, "{3}",
                List.of(new BoostTargetCreatureEffect(
                        new BasicLandTypesAmongControlledLands(),
                        new BasicLandTypesAmongControlledLands())),
                "Domain — {3}, {T}: Target creature gets +1/+1 until end of turn for each basic land type among lands you control."));
    }
}
