package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "YMKM", collectorNumber = "5")
public class EmporiumThopterist extends Card {

    public EmporiumThopterist() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.THOPTER)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConjureCardNamedIntoHandEffect("Ornithopter", false));
    }
}
