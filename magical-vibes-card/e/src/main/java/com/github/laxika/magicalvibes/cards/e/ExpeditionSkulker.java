package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsOtherPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ZNR", collectorNumber = "101")
public class ExpeditionSkulker extends Card {

    public ExpeditionSkulker() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsOtherPermanentCount(1, new PermanentHasSubtypePredicate(CardSubtype.ROGUE)),
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF)
        ));
    }
}
