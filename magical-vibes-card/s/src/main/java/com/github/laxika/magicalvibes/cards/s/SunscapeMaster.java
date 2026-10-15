package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "42")
public class SunscapeMaster extends Card {

    public SunscapeMaster() {
        // {G}{G}, {T}: Creatures you control get +2/+2 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}{G}",
                List.of(new BoostAllOwnCreaturesEffect(2, 2)),
                "{G}{G}, {T}: Creatures you control get +2/+2 until end of turn."
        ));

        // {U}{U}, {T}: Return target creature to its owner's hand.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}{U}",
                List.of(ReturnToHandEffect.target()),
                "{U}{U}, {T}: Return target creature to its owner's hand.",
                TargetFilters.creature()
        ));
    }
}
