package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.DuringCombat;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastOrActivateDuringYourTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.effect.Subjects;

/**
 * Hand to Hand — "During combat, players can't cast instant spells or activate abilities that
 * aren't mana abilities."
 */
@CardRegistration(set = "TMP", collectorNumber = "180")
public class HandToHand extends Card {

    public HandToHand() {
        addStaticEffect(new OpponentsCantCastOrActivateDuringYourTurnEffect(Subjects.eachPlayer(),
                PlayerRestriction.castingSpellsOf(CardType.INSTANT), PlayerRestriction.activatingNonManaAbilities(),
                new DuringCombat()));
    }
}
