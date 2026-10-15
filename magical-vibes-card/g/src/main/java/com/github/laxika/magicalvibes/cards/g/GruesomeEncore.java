package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect;

@CardRegistration(set = "MBS", collectorNumber = "44")
public class GruesomeEncore extends Card {

    public GruesomeEncore() {
        addEffect(EffectSlot.SPELL, new PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect());
    }
}
