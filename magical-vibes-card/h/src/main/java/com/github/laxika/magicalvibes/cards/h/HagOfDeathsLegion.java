package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "H19")
public class HagOfDeathsLegion extends Card {

    public HagOfDeathsLegion() {
        // At the beginning of your upkeep, create a 1/1 black Skeleton creature token.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CreateTokenEffect("Skeleton", 1, 1, CardColor.BLACK,
                List.of(CardSubtype.SKELETON), Set.of(), Set.of()));
    }
}
