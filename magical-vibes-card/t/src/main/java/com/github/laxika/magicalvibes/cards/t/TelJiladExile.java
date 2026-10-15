package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "133")
public class TelJiladExile extends Card {

    public TelJiladExile() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{G}", List.of(new RegenerateEffect()),
                "{1}{G}: Regenerate Tel-Jilad Exile."));
    }
}
