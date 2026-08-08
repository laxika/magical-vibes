package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

/**
 * One with the Machine — {3}{U} sorcery: draw cards equal to the greatest mana value among
 * artifacts you control.
 */
@CardRegistration(set = "M19", collectorNumber = "66")
public class OneWithTheMachine extends Card {

    public OneWithTheMachine() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(
                new GreatestManaValueAmongControlled(new PermanentIsArtifactPredicate())));
    }
}
