package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "53")
public class Glimmerbell extends Card {

    public Glimmerbell() {
        // {1}{U}: Untap this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new UntapPermanentsEffect(TapUntapScope.SELF)),
                "{1}{U}: Untap Glimmerbell."
        ));
    }
}
