package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;

@CardRegistration(set = "5ED", collectorNumber = "32")
public class Heal extends Card {

    public Heal() {
        // "Prevent the next 1 damage that would be dealt to any target this turn."
        addEffect(EffectSlot.SPELL, new PreventDamageToTargetEffect(1));
        // "Draw a card at the beginning of the next turn's upkeep."
        addEffect(EffectSlot.SPELL, new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
