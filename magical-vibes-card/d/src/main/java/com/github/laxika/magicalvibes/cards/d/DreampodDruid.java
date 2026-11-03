package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.Enchanted;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VMA", collectorNumber = "204")
@CardRegistration(set = "PC2", collectorNumber = "64")
@CardRegistration(set = "PCA", collectorNumber = "64")
public class DreampodDruid extends Card {

    public DreampodDruid() {
        // At the beginning of each upkeep, if this creature is enchanted, create a 1/1 green
        // Saproling creature token.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new Enchanted()),
                new CreateTokenEffect("Saproling", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.SAPROLING), Set.of(), Set.of()));
    }
}
