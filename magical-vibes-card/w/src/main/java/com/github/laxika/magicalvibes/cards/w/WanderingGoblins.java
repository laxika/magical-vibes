package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "CON", collectorNumber = "76")
public class WanderingGoblins extends Card {

    public WanderingGoblins() {
        // Domain — {3}: This creature gets +1/+0 until end of turn for each basic land type among lands you control.
        addActivatedAbility(new ActivatedAbility(false, "{3}",
                List.of(new BoostSelfEffect(new BasicLandTypesAmongControlledLands(), new Fixed(0))),
                "Domain — {3}: This creature gets +1/+0 until end of turn for each basic land type among lands you control."));
    }
}
