package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TurnSourceFaceDownEffect;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "55")
@CardRegistration(set = "DD2", collectorNumber = "5")
@CardRegistration(set = "JVC", collectorNumber = "5")
public class WallOfDeceit extends Card {

    public WallOfDeceit() {
        addActivatedAbility(new ActivatedAbility(false, "{3}", List.of(new TurnSourceFaceDownEffect()),
                "{3}: Turn this creature face down."));
        addMorph("{U}");
    }
}
