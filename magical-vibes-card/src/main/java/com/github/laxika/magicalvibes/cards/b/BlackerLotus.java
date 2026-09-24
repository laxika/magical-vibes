package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "869")
public class BlackerLotus extends Card {

    public BlackerLotus() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ExileSelfEffect(), new AwardAnyColorManaEffect(4)),
                "{T}: Tear this artifact into pieces. Add four mana of any one color. Remove the pieces from the game."
        ));
    }
}
