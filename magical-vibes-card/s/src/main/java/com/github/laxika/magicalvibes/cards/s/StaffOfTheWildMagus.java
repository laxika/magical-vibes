package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M14", collectorNumber = "223")
@CardRegistration(set = "M15", collectorNumber = "236")
public class StaffOfTheWildMagus extends Card {

    public StaffOfTheWildMagus() {
        // Whenever you cast a green spell or a Forest you control enters, you gain 1 life.
        addTrigger(Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.GREEN)), new GainLifeEffect(1));

        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.LAND,
                                new PermanentHasSubtypePredicate(CardSubtype.FOREST))),
                        ObjectRelation.YOU_CONTROL),
                new GainLifeEffect(1));
    }
}
