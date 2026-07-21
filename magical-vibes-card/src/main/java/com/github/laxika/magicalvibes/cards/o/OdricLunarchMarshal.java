package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "36")
public class OdricLunarchMarshal extends Card {

    private static final List<Keyword> SHARED_KEYWORDS = List.of(
            Keyword.FIRST_STRIKE,
            Keyword.FLYING,
            Keyword.DEATHTOUCH,
            Keyword.DOUBLE_STRIKE,
            Keyword.HASTE,
            Keyword.HEXPROOF,
            Keyword.INDESTRUCTIBLE,
            Keyword.LIFELINK,
            Keyword.MENACE,
            Keyword.REACH,
            Keyword.SKULK,
            Keyword.TRAMPLE,
            Keyword.VIGILANCE
    );

    public OdricLunarchMarshal() {
        // At the beginning of each combat, creatures you control gain [keyword] until end of turn
        // if a creature you control has that keyword. Not intervening-if — ability always triggers;
        // each keyword check is at resolution (Gatherer).
        for (Keyword keyword : SHARED_KEYWORDS) {
            addEffect(EffectSlot.EACH_BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                    new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                            new PermanentIsCreaturePredicate(),
                            new PermanentHasKeywordPredicate(keyword)
                    ))),
                    new GrantKeywordEffect(keyword, GrantScope.OWN_CREATURES)
            ));
        }
    }
}
