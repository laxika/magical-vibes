package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "117")
@CardRegistration(set = "SLD", collectorNumber = "1250")
@CardRegistration(set = "2XM", collectorNumber = "205")
public class KarrthusTyrantOfJund extends Card {

    public KarrthusTyrantOfJund() {
        // When Karrthus enters, gain control of all Dragons, then untap all Dragons.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new GainControlOfAllPermanentsMatchingEffect(new PermanentHasSubtypePredicate(CardSubtype.DRAGON)),
                new UntapPermanentsEffect(TapUntapScope.ALL_PERMANENTS, new PermanentHasSubtypePredicate(CardSubtype.DRAGON)));

        // Other Dragon creatures you control have haste.
        addStaticEffect(new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.DRAGON)));
    }
}
