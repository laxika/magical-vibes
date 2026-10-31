package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;

@CardRegistration(set = "TOR", collectorNumber = "10")
public class Morningtide extends Card {

    public Morningtide() {
        addSpellEffect(new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_PLAYERS));
    }
}
