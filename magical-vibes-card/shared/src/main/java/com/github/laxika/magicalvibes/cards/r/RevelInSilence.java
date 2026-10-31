package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantActivatePlaneswalkerLoyaltyAbilitiesThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsThisTurnEffect;

public class RevelInSilence extends Card {

    public RevelInSilence() {
        addSpellEffect(new OpponentsCantCastSpellsThisTurnEffect());
        addSpellEffect(new OpponentsCantActivatePlaneswalkerLoyaltyAbilitiesThisTurnEffect());
        addSpellEffect(new ExileSpellEffect());
    }
}
