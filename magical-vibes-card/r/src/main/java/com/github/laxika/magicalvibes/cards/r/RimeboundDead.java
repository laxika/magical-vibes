package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "69")
public class RimeboundDead extends Card {

    public RimeboundDead() {
        addActivatedAbility(new ActivatedAbility(false, "{S}",
                List.of(new RegenerateEffect()),
                "{S}: Regenerate Rimebound Dead."));
    }
}
