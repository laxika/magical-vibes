package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SOM", collectorNumber = "204")
public class Soliton extends Card {

    public Soliton() {
        addActivatedAbility(new ActivatedAbility(false, "{U}", List.of(new UntapPermanentsEffect(TapUntapScope.SELF)), "{U}: Untap Soliton."));
    }
}
