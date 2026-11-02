package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "WOC", collectorNumber = "1")
@CardRegistration(set = "WOC", collectorNumber = "39")
@CardRegistration(set = "WOC", collectorNumber = "58")
public class TegwyllDukeOfSplendor extends Card {

    private static final SequenceEffect DEATH_TRIGGER = SequenceEffect.of(
            new DrawCardEffect(1),
            new LoseLifeEffect(1));

    public TegwyllDukeOfSplendor() {
        // Other Faeries you control get +1/+1.
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.FAERIE))));

        // Whenever another Faerie you control dies, you draw a card and you lose 1 life.
        addTrigger(Triggers.creatureDies(
                        new PermanentHasSubtypePredicate(CardSubtype.FAERIE),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                DEATH_TRIGGER);
    }
}
