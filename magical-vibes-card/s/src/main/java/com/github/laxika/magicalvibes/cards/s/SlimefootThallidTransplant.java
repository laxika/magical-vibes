package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DraftSlimefootThallidTransplantSpellbookEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YDMU", collectorNumber = "26")
public class SlimefootThallidTransplant extends Card {

    public SlimefootThallidTransplant() {
        // Whenever a Swamp or Forest enters under your control, draft a card from this creature's
        // spellbook.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.LAND,
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentHasSubtypePredicate(CardSubtype.SWAMP),
                                        new PermanentHasSubtypePredicate(CardSubtype.FOREST))))),
                        ObjectRelation.YOU_CONTROL),
                new DraftSlimefootThallidTransplantSpellbookEffect());
    }
}
