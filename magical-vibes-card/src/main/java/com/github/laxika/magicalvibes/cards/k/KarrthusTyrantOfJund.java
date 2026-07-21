package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ARB", collectorNumber = "117")
public class KarrthusTyrantOfJund extends Card {

    public KarrthusTyrantOfJund() {
        // When Karrthus enters, gain control of all Dragons, then untap all Dragons.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GainControlOfAllPermanentsMatchingEffect(new PermanentHasSubtypePredicate(CardSubtype.DRAGON)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new UntapPermanentsEffect(TapUntapScope.ALL_CREATURES, new PermanentHasSubtypePredicate(CardSubtype.DRAGON)));

        // Other Dragon creatures you control have haste.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.DRAGON)));
    }
}
