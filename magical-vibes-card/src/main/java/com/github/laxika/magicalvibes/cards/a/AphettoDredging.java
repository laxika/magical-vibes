package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreaturesOfChosenTypeFromGraveyardToHandEffect;

@CardRegistration(set = "ONS", collectorNumber = "125")
public class AphettoDredging extends Card {

    public AphettoDredging() {
        addEffect(EffectSlot.SPELL, new ReturnTargetCreaturesOfChosenTypeFromGraveyardToHandEffect(3));
    }
}
