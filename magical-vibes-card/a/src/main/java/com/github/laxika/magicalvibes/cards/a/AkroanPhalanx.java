package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

import java.util.List;

@CardRegistration(set = "BNG", collectorNumber = "2")
public class AkroanPhalanx extends Card {

    public AkroanPhalanx() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new BoostAllOwnCreaturesEffect(1, 0)),
                "{2}{R}: Creatures you control get +1/+0 until end of turn."));
    }
}
