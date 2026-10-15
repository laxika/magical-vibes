package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellIfNameFoundElsewhereEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;

@CardRegistration(set = "MIR", collectorNumber = "55")
public class BazaarOfWonders extends Card {

    public BazaarOfWonders() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_PLAYERS));
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new CounterSpellIfNameFoundElsewhereEffect());
    }
}
