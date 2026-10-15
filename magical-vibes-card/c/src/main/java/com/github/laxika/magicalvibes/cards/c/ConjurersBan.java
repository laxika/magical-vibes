package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameSpellsAndLandsCantBePlayedUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "GPT", collectorNumber = "108")
public class ConjurersBan extends Card {

    public ConjurersBan() {
        addEffect(EffectSlot.SPELL, new ChooseCardNameSpellsAndLandsCantBePlayedUntilNextTurnEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
