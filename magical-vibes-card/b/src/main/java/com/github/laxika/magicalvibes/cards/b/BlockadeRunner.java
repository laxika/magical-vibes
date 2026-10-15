package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "60")
public class BlockadeRunner extends Card {

    public BlockadeRunner() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new MakeCreatureUnblockableEffect(true)),
                "{U}: This creature can't be blocked this turn."
        ));
    }
}
