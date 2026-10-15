package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.NextPlayerGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerDirection;
import com.github.laxika.magicalvibes.model.effect.RollD4Effect;

import java.util.List;

@CardRegistration(set = "AFC", collectorNumber = "57")
public class BucknardsEverfullPurse extends Card {

    public BucknardsEverfullPurse() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new RollD4Effect(null, CreateTokenEffect.ofTreasureToken(new EventValue())),
                        new NextPlayerGainsControlOfSourceEffect(PlayerDirection.RIGHT)),
                "{1}, {T}: Roll a d4 and create a number of Treasure tokens equal to the result. "
                        + "The player to your right gains control of this artifact."));
    }
}
