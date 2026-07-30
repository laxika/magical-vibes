package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "73")
public class EronTheRelentless extends Card {

    public EronTheRelentless() {
        // Haste is auto-loaded from Scryfall keywords.
        addActivatedAbility(new ActivatedAbility(false, "{R}{R}{R}", List.of(new RegenerateEffect()), "{R}{R}{R}: Regenerate Eron the Relentless."));
    }
}
