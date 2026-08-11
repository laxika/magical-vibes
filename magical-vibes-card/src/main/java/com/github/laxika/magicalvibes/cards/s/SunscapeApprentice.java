package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "41")
public class SunscapeApprentice extends Card {

    public SunscapeApprentice() {
        addActivatedAbility(new ActivatedAbility(true, "{G}",
                List.of(new BoostTargetCreatureEffect(1, 1)),
                "{G}, {T}: Target creature gets +1/+1 until end of turn.",
                TargetFilters.creature()));

        addActivatedAbility(new ActivatedAbility(true, "{U}",
                List.of(new PutTargetOnTopOfLibraryEffect()),
                "{U}, {T}: Put target creature you control on top of its owner's library.",
                TargetFilters.creatureYouControl()));
    }
}
