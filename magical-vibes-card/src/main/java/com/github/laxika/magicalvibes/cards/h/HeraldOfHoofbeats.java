package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MOC", collectorNumber = "22")
@CardRegistration(set = "MOC", collectorNumber = "109")
public class HeraldOfHoofbeats extends Card {

    public HeraldOfHoofbeats() {
        // Other Knights you control have horsemanship.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HORSEMANSHIP, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.KNIGHT)));
    }
}
