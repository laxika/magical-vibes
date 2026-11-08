package com.github.laxika.magicalvibes.cards.o;

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

@CardRegistration(set = "M14", collectorNumber = "148")
@CardRegistration(set = "DDN", collectorNumber = "16")
@CardRegistration(set = "ANB", collectorNumber = "80")
@CardRegistration(set = "TDC", collectorNumber = "227")
public class OgreBattledriver extends Card {

    public OgreBattledriver() {
        // Whenever another creature you control enters, that creature gets +2/+0 and gains haste until end of turn.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                SequenceEffect.of(
                        new BoostReferencedPermanentEffect(PermanentReference.TRIGGERING, 2, 0),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TRIGGERING_PERMANENT)));
    }
}
