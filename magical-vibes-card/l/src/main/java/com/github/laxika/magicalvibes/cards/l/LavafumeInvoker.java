package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "155")
public class LavafumeInvoker extends Card {

    public LavafumeInvoker() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{8}",
                List.of(new BoostAllOwnCreaturesEffect(3, 0)),
                "{8}: Creatures you control get +3/+0 until end of turn."
        ));
    }
}
