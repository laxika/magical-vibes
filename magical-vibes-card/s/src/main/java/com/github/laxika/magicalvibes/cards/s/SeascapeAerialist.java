package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "64")
public class SeascapeAerialist extends Card {

    public SeascapeAerialist() {
        // Whenever this creature or another Ally you control enters, you may have Ally creatures
        // you control gain flying until end of turn. The entering card is checked against "this
        // creature" or "Ally", so a Conspiracy-style type change does not stop the source's own entry.
        // The grant covers Allies you control at resolution, which includes the source only while
        // you still control it.
        PermanentHasSubtypePredicate ally = new PermanentHasSubtypePredicate(CardSubtype.ALLY);
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.ALLY))),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                new MayEffect(
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.ALL_OWN_CREATURES, ally),
                        "Have Ally creatures you control gain flying until end of turn?"));
    }
}
