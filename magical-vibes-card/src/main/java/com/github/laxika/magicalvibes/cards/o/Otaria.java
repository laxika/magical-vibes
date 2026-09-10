package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToGraveyardCardsEffect;

import java.util.Set;

@CardRegistration(set = "OHOP", collectorNumber = "28")
public class Otaria extends Card {

    public Otaria() {
        addEffect(EffectSlot.STATIC, new GrantFlashbackToGraveyardCardsEffect(
                Set.of(CardType.INSTANT, CardType.SORCERY), true));
        addEffect(EffectSlot.CHAOS_TRIGGERED, new ControllerExtraTurnEffect(1));
    }
}
