package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "69")
public class NakayaShade extends Card {

    public NakayaShade() {
        // {B}: This creature gets +1/+1 until end of turn unless any player pays {2}.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new ForcedCostOrElseEffect(
                        new PayManaCost("{2}"),
                        List.of(new BoostSelfEffect(1, 1)),
                        true,
                        true)),
                "{B}: This creature gets +1/+1 until end of turn unless any player pays {2}."
        ));
    }
}
