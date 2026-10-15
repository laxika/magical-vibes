package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "241")
public class SeedcradleWitch extends Card {

    public SeedcradleWitch() {
        addActivatedAbility(new ActivatedAbility(false, "{2}{G}{W}",
                List.of(new BoostTargetCreatureEffect(3, 3),
                        new UntapPermanentsEffect(TapUntapScope.TARGET)),
                "{2}{G}{W}: Target creature gets +3/+3 until end of turn. Untap that creature."));
    }
}
