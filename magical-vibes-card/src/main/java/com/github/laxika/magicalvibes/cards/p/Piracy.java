package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayTapLandsYouDontControlForSpellsUntilEndOfTurnEffect;

@CardRegistration(set = "P02", collectorNumber = "42")
public class Piracy extends Card {

    public Piracy() {
        addEffect(EffectSlot.SPELL, new MayTapLandsYouDontControlForSpellsUntilEndOfTurnEffect());
    }
}
