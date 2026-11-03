package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayUntilAnotherEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "96")
@CardRegistration(set = "OM1", collectorNumber = "91")
@CardRegistration(set = "MSC", collectorNumber = "167")
public class SuperiorFoesOfSpiderMan extends Card {

    public SuperiorFoesOfSpiderMan() {
        // Whenever you cast a spell with mana value 4 or greater, you may exile the top card of
        // your library. If you do, you may play that card until you exile another card with this creature.
        addTrigger(Triggers.youCastSpell().withFilter(new CardMinManaValuePredicate(4)),
                new MayEffect(new ExileTopCardMayPlayUntilAnotherEffect(), "Exile the top card of your library?"));
    }
}
