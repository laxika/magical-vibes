package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "11")
public class Aetherling extends Card {

    public Aetherling() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(FlickerEffect.exileSelfReturnAtEndStep()),
                "{U}: Exile Aetherling. Return it to the battlefield under its owner's control at the beginning of the next end step."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new MakeCreatureUnblockableEffect(true)),
                "{U}: Aetherling can't be blocked this turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new BoostSelfEffect(1, -1)),
                "{1}: Aetherling gets +1/-1 until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new BoostSelfEffect(-1, 1)),
                "{1}: Aetherling gets -1/+1 until end of turn."
        ));
    }
}
