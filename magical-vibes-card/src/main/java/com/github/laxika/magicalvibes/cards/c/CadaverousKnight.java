package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "110")
public class CadaverousKnight extends Card {

    public CadaverousKnight() {
        // Flanking is auto-loaded from Scryfall keywords.
        addActivatedAbility(new ActivatedAbility(false, "{1}{B}{B}", List.of(new RegenerateEffect()), "{1}{B}{B}: Regenerate Cadaverous Knight."));
    }
}
