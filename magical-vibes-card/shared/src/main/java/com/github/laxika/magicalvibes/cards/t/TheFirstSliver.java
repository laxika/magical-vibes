package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "1371")
@CardRegistration(set = "MH1", collectorNumber = "200")
public class TheFirstSliver extends Card {

    public TheFirstSliver() {
        addTrigger(Triggers.youCastThisSpell(), new CascadeEffect());
        addTrigger(Triggers.youCastSpell().withFilter(new CardSubtypePredicate(CardSubtype.SLIVER)).grantedToTheSpell(),
                new CascadeEffect());
    }
}
