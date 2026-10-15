package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;

@CardRegistration(set = "ONS", collectorNumber = "144")
public class FadeFromMemory extends Card {

    public FadeFromMemory() {
        addEffect(EffectSlot.SPELL, new ExileGraveyardCardsEffect(
                1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD));
        addCycling("{B}");
    }
}
