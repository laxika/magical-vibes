package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;

@CardRegistration(set = "KLD", collectorNumber = "174")
@CardRegistration(set = "KLR", collectorNumber = "187")
public class WildestDreams extends Card {

    public WildestDreams() {
        addSpellEffect(new ReturnTargetCardsFromGraveyardToHandEffect(null, 0, true));
        addSpellEffect(new ExileSpellEffect());
    }
}
