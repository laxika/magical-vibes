package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "213")
public class MyrMindservant extends Card {

    public MyrMindservant() {
        // {2}, {T}: Shuffle your library.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new ShuffleLibraryEffect(false)),
                "{2}, {T}: Shuffle your library."
        ));
    }
}
