package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "M12", collectorNumber = "20")
public class GriffinRider extends Card {

    public GriffinRider() {
        // As long as you control a Griffin creature, this creature gets +3/+3 and has flying.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.GRIFFIN)), new StaticBoostEffect(3, 3, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.GRIFFIN)), new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));
    }
}
