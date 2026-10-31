package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardXCardsCost;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;

@CardRegistration(set = "TOR", collectorNumber = "135")
@CardRegistration(set = "VMA", collectorNumber = "222")
public class NostalgicDreams extends Card {

    public NostalgicDreams() {
        addCastingCost(new DiscardXCardsCost());
        addSpellEffect(new ReturnTargetCardsFromGraveyardToHandEffect(null, 0, true));
        addSpellEffect(new ExileSpellEffect());
    }
}
