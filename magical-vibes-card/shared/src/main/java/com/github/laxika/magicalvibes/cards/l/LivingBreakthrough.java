package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.effect.PlayerRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardManaValueEqualsTriggeringSpellPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


public class LivingBreakthrough extends Card {

    public LivingBreakthrough() {
        addTrigger(Triggers.youCastSpell().anySpell(), new PlayerRestrictionEffect(Subjects.eachOpponent(),
                PlayerRestriction.castingSpellsMatching(new CardManaValueEqualsTriggeringSpellPredicate()),
                Duration.UNTIL_YOUR_NEXT_TURN));
    }
}
