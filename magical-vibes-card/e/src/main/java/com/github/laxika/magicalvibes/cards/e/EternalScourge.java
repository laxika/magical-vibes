package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExileCast;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EMN", collectorNumber = "7")
@CardRegistration(set = "SIR", collectorNumber = "7")
public class EternalScourge extends Card {

    public EternalScourge() {
        // You may cast this card from exile.
        addCastingOption(new ExileCast());

        // When this creature becomes the target of a spell or ability an opponent controls, exile
        // this creature.
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new ExileSelfEffect());
    }
}
