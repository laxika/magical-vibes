package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "346")
@CardRegistration(set = "DKM", collectorNumber = "38")
public class WalkingWall extends Card {

    public WalkingWall() {
        // Defender is auto-loaded from Scryfall keywords.
        addActivatedAbility(new ActivatedAbility(false, "{3}",
                List.of(new BoostSelfEffect(3, -1), new CanAttackAsThoughNoDefenderEffect()),
                "{3}: This creature gets +3/-1 until end of turn and can attack this turn as though it didn't have defender. Activate only once each turn.",
                1));
    }
}
