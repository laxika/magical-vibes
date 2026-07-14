package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;

import java.util.List;

@CardRegistration(set = "7ED", collectorNumber = "112")
public class WallOfWonder extends Card {

    public WallOfWonder() {
        // Defender is auto-loaded from Scryfall keywords.
        addActivatedAbility(new ActivatedAbility(false, "{2}{U}{U}",
                List.of(new BoostSelfEffect(4, -4), new CanAttackAsThoughNoDefenderEffect()),
                "{2}{U}{U}: This creature gets +4/-4 until end of turn and can attack this turn as though it didn't have defender."));
    }
}
