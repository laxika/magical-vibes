package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "268")
@CardRegistration(set = "ATH", collectorNumber = "66")
public class RangerEnVec extends Card {

    public RangerEnVec() {
        // First strike is auto-loaded from Scryfall keywords.
        addActivatedAbility(new ActivatedAbility(false, "{G}", List.of(new RegenerateEffect()), "{G}: Regenerate Ranger en-Vec."));
    }
}
