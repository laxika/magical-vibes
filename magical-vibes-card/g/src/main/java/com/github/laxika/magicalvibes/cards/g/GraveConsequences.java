package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayExileGraveyardCardsThenLoseLifeEffect;

@CardRegistration(set = "JUD", collectorNumber = "67")
public class GraveConsequences extends Card {

    public GraveConsequences() {
        addSpellEffect(new EachPlayerMayExileGraveyardCardsThenLoseLifeEffect());
        addSpellEffect(new DrawCardEffect(1));
    }
}
