package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllowPlayFromOtherGraveyardsThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.OtherPlayersCantPlayFromGraveyardsThisTurnEffect;

@CardRegistration(set = "JUD", collectorNumber = "98")
public class ShamansTrance extends Card {

    public ShamansTrance() {
        addSpellEffect(new OtherPlayersCantPlayFromGraveyardsThisTurnEffect());
        addSpellEffect(new AllowPlayFromOtherGraveyardsThisTurnEffect());
    }
}
