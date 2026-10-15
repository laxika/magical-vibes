package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentInCombatWithSourcePredicate;

@CardRegistration(set = "GTC", collectorNumber = "141")
public class AlmsBeast extends Card {

    public AlmsBeast() {
        // ALL_CREATURES excludes the source, so the Beast itself never picks up lifelink.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.ALL_CREATURES,
                new PermanentInCombatWithSourcePredicate()));
    }
}
