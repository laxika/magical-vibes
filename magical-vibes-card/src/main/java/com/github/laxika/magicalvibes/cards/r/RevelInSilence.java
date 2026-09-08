package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantActivatePlaneswalkerLoyaltyAbilitiesThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsThisTurnEffect;

public class RevelInSilence extends Card {

    public RevelInSilence() {
        addEffect(EffectSlot.SPELL, new OpponentsCantCastSpellsThisTurnEffect());
        addEffect(EffectSlot.SPELL, new OpponentsCantActivatePlaneswalkerLoyaltyAbilitiesThisTurnEffect());
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
