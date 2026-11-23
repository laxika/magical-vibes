package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.effect.PlayerRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantActivatePlaneswalkerLoyaltyAbilitiesThisTurnEffect;

public class RevelInSilence extends Card {

    public RevelInSilence() {
        addSpellEffect(PlayerRestrictionEffect.thisTurn(Subjects.eachOpponent(),
                PlayerRestriction.castingSpells()));
        addSpellEffect(new OpponentsCantActivatePlaneswalkerLoyaltyAbilitiesThisTurnEffect());
        addSpellEffect(new ExileSpellEffect());
    }
}
