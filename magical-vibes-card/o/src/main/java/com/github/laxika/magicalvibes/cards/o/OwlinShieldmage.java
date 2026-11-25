package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "STX", collectorNumber = "210")
public class OwlinShieldmage extends Card {

    public OwlinShieldmage() {
        // Flying is auto-loaded from Scryfall.
        // Ward—Pay 3 life.
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                UnlessPaysEffect.counterUnlessPaysLife(3));
    }
}
