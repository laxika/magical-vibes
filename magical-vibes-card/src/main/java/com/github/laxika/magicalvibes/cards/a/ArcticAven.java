package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
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

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "42")
public class ArcticAven extends Card {

    public ArcticAven() {
        // Arctic Aven gets +1/+1 as long as you control a Plains.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.PLAINS)),
                new StaticBoostEffect(1, 1, GrantScope.SELF)));

        // {W}: Arctic Aven gains lifelink until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{W}",
                List.of(new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF)),
                "{W}: Arctic Aven gains lifelink until end of turn."));
    }
}
