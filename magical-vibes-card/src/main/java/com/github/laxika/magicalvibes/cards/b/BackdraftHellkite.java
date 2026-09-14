package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToGraveyardCardsEffect;

import java.util.Set;

@CardRegistration(set = "2X2", collectorNumber = "103")
public class BackdraftHellkite extends Card {

    public BackdraftHellkite() {
        addEffect(EffectSlot.ON_ATTACK, new GrantFlashbackToGraveyardCardsEffect(
                Set.of(CardType.INSTANT, CardType.SORCERY)));
    }
}
