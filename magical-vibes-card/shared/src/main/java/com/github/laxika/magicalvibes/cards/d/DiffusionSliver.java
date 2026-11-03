package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M15", collectorNumber = "50")
@CardRegistration(set = "SLD", collectorNumber = "618")
public class DiffusionSliver extends Card {

    public DiffusionSliver() {
        addTrigger(Triggers.creatureYouControlBecomesTargetOfOpponentSpellOrAbility()
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.SLIVER)),
                new CounterUnlessPaysEffect(2));
    }
}
