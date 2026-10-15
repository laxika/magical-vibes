package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "BNG", collectorNumber = "81")
public class Sanguimancy extends Card {

    public Sanguimancy() {
        ColorManaSymbolsAmongControlledPermanents blackDevotion =
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.BLACK);
        addEffect(EffectSlot.SPELL, new DrawCardEffect(blackDevotion));
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(blackDevotion, LoseLifeRecipient.CONTROLLER));
    }
}
