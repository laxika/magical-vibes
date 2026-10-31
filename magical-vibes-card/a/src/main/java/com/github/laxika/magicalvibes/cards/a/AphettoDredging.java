package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreaturesOfChosenTypeFromGraveyardToHandEffect;

@CardRegistration(set = "ONS", collectorNumber = "125")
@CardRegistration(set = "H09", collectorNumber = "28")
public class AphettoDredging extends Card {

    public AphettoDredging() {
        addSpellEffect(new ReturnTargetCreaturesOfChosenTypeFromGraveyardToHandEffect(3));
    }
}
