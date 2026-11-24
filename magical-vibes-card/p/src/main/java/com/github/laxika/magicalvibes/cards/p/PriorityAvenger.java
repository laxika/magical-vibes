package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.StackIsEmpty;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastOrActivateDuringYourTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MB1", collectorNumber = "9")
public class PriorityAvenger extends Card {

    public PriorityAvenger() {
        addStaticEffect(new OpponentsCantCastOrActivateDuringYourTurnEffect(Subjects.eachPlayer(),
                PlayerRestriction.castingSpellsOf(CardType.INSTANT), null, new StackIsEmpty()));
    }
}
