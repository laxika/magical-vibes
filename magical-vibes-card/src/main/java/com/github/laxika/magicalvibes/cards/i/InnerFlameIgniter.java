package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.InnerFlameIgniterEffect;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "182")
public class InnerFlameIgniter extends Card {

    public InnerFlameIgniter() {
        // {2}{R}: Creatures you control get +1/+0 until end of turn. If this is the third time this
        // ability has resolved this turn, creatures you control gain first strike until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new BoostAllOwnCreaturesEffect(1, 0), new InnerFlameIgniterEffect()),
                "{2}{R}: Creatures you control get +1/+0 until end of turn. If this is the third time this ability has resolved this turn, creatures you control gain first strike until end of turn."
        ));
    }
}
