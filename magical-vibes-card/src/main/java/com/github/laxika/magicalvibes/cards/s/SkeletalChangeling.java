package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "LRW", collectorNumber = "140")
public class SkeletalChangeling extends Card {

    public SkeletalChangeling() {
        // Changeling is auto-loaded from Scryfall metadata.
        addActivatedAbility(new ActivatedAbility(false, "{1}{B}", List.of(new RegenerateEffect()), "{1}{B}: Regenerate this creature."));
    }
}
