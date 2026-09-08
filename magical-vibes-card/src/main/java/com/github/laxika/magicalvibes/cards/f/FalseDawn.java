package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceColoredManaWithWhiteUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpendWhiteManaAsAnyColorUntilEndOfTurnEffect;

@CardRegistration(set = "APC", collectorNumber = "10")
public class FalseDawn extends Card {

    public FalseDawn() {
        addEffect(EffectSlot.SPELL, new ReplaceColoredManaWithWhiteUntilEndOfTurnEffect());
        addEffect(EffectSlot.SPELL, new SpendWhiteManaAsAnyColorUntilEndOfTurnEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
