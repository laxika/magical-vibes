package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BOK", collectorNumber = "108")
public class InTheWebOfWar extends Card {

    public InTheWebOfWar() {
        // Whenever a creature you control enters, it gets +2/+0 and gains haste until end of turn.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                SequenceEffect.of(
                        new BoostReferencedPermanentEffect(PermanentReference.TRIGGERING, 2, 0),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TRIGGERING_PERMANENT)));
    }
}
