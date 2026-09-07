package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToNextSpellOfTypeThisTurnEffect;

@CardRegistration(set = "FUT", collectorNumber = "16")
public class ScoutsWarning extends Card {

    public ScoutsWarning() {
        addEffect(EffectSlot.SPELL, new GrantFlashToNextSpellOfTypeThisTurnEffect(CardType.CREATURE));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
