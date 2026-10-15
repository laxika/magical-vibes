package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardBisonWhistleEffect;

import java.util.List;

@CardRegistration(set = "TLE", collectorNumber = "131")
@CardRegistration(set = "TLE", collectorNumber = "202")
public class BisonWhistle extends Card {

    public BisonWhistle() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new LookAtTopCardBisonWhistleEffect()),
                "{1}, {T}: Look at the top card of your library. If it's a Bison card, you may put it onto the battlefield. If it's a creature card, you may reveal it and put it into your hand. Otherwise, you may put it into your graveyard."
        ));
    }
}
