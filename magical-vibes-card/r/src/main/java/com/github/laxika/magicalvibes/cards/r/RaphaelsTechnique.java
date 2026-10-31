package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDiscardHandThenDrawEffect;

@CardRegistration(set = "TMT", collectorNumber = "105")
@CardRegistration(set = "TMT", collectorNumber = "237")
public class RaphaelsTechnique extends Card {

    public RaphaelsTechnique() {
        addSneak("{2}{R}");
        addSpellEffect(new EachPlayerMayDiscardHandThenDrawEffect(7));
    }
}
