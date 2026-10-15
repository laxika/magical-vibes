package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "257")
@CardRegistration(set = "TPR", collectorNumber = "193")
public class SkyshroudTroll extends Card {

    public SkyshroudTroll() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{G}", List.of(new RegenerateEffect()), "{1}{G}: Regenerate Skyshroud Troll."));
    }
}
