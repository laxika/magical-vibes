package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceIsFaceDown;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "11")
public class DefenestratedPhantom extends Card {

    public DefenestratedPhantom() {
        addMorph("{4}{W}");
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                new ConditionalEffect(new SourceIsFaceDown(), new CounterUnlessPaysEffect(2)));
    }
}
