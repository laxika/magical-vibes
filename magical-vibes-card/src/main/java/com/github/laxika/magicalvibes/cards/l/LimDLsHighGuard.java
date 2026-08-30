package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "55a")
@CardRegistration(set = "ALL", collectorNumber = "55b")
@CardRegistration(set = "DKM", collectorNumber = "6")
public class LimDLsHighGuard extends Card {

    public LimDLsHighGuard() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{B}", List.of(new RegenerateEffect()),
                "{1}{B}: Regenerate Lim-Dûl's High Guard."));
    }
}
