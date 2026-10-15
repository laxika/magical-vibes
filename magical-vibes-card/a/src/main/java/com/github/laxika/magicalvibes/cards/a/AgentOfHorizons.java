package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "148")
public class AgentOfHorizons extends Card {

    public AgentOfHorizons() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new MakeCreatureUnblockableEffect(true)),
                "{2}{U}: This creature can't be blocked this turn."
        ));
    }
}
