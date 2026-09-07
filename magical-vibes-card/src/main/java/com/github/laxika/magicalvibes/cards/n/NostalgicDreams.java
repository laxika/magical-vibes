package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardXCardsCost;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;

@CardRegistration(set = "TOR", collectorNumber = "135")
public class NostalgicDreams extends Card {

    public NostalgicDreams() {
        addEffect(EffectSlot.SPELL, new DiscardXCardsCost());
        addEffect(EffectSlot.SPELL, new ReturnTargetCardsFromGraveyardToHandEffect(null, 0, true));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
