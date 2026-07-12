package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "59")
public class Cinderbones extends Card {

    public Cinderbones() {
        // Wither is auto-loaded from Scryfall keywords.
        addActivatedAbility(new ActivatedAbility(false, "{1}{B}", List.of(new RegenerateEffect()), "{1}{B}: Regenerate Cinderbones."));
    }
}
