package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "338")
public class WallOfBrambles extends Card {

    public WallOfBrambles() {
        // {G}: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(false, "{G}",
                List.of(new RegenerateEffect()),
                "{G}: Regenerate Wall of Brambles."));
    }
}
