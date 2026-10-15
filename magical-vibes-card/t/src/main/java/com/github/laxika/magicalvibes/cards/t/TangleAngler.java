package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SOM", collectorNumber = "128")
public class TangleAngler extends Card {

    public TangleAngler() {
        addActivatedAbility(new ActivatedAbility(false, "{G}", List.of(new MustBlockSourceEffect(null)), "{G}: Target creature blocks Tangle Angler this turn if able."));
    }
}
