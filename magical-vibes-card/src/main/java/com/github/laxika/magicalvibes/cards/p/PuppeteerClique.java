package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect;

@CardRegistration(set = "SHM", collectorNumber = "75")
public class PuppeteerClique extends Card {

    public PuppeteerClique() {
        // Flying and Persist are auto-loaded from Scryfall (Persist is handled by the engine).
        // When this creature enters, put target creature card from an opponent's graveyard onto
        // the battlefield under your control. It gains haste. At the beginning of your next end
        // step, exile it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect());
    }
}
