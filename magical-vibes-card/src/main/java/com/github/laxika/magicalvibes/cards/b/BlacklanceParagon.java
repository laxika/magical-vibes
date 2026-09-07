package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "79")
public class BlacklanceParagon extends Card {

    public BlacklanceParagon() {
        PermanentHasSubtypePredicate knight = new PermanentHasSubtypePredicate(CardSubtype.KNIGHT);
        target(new PermanentPredicateTargetFilter(knight, "Target must be a Knight"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new GrantKeywordEffect(Set.of(Keyword.DEATHTOUCH, Keyword.LIFELINK), GrantScope.TARGET, knight));
    }
}
