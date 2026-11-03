package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MUL", collectorNumber = "43")
@CardRegistration(set = "MUL", collectorNumber = "108")
@CardRegistration(set = "MUL", collectorNumber = "173")
public class ImotiCelebrantOfBounty extends Card {

    public ImotiCelebrantOfBounty() {
        // Cascade when Imoti is cast.
        addTrigger(Triggers.youCastThisSpell(), new CascadeEffect());

        // Spells with mana value 6 or greater that its controller casts have cascade. The generic
        // spell-cast collector snapshots the triggering spell's mana value for CascadeEffect.
        addTrigger(Triggers.youCastSpell().withFilter(new CardMinManaValuePredicate(6, true)), new CascadeEffect());
    }
}
