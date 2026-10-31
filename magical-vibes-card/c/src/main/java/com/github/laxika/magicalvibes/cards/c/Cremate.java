package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;

@CardRegistration(set = "RTR", collectorNumber = "59")
@CardRegistration(set = "INV", collectorNumber = "96")
@CardRegistration(set = "GPT", collectorNumber = "45")
public class Cremate extends Card {

    public Cremate() {
        addSpellEffect(new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD));
        addSpellEffect(new DrawCardEffect());
    }
}
