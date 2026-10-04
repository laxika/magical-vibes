package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "274")
@CardRegistration(set = "MB2", collectorNumber = "510")
public class BrigidWhosSeenSomeStuff extends Card {

    public BrigidWhosSeenSomeStuff() {
        var kithkin = new PermanentHasSubtypePredicate(CardSubtype.KITHKIN);
        for (Keyword keyword : Keyword.values()) {
            addEffect(EffectSlot.STATIC, new ConditionalEffect(
                    new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                            kithkin, new PermanentHasKeywordPredicate(keyword, true)))),
                    new GrantKeywordEffect(keyword, GrantScope.ALL_OWN_CREATURES, kithkin)));
        }
    }
}
