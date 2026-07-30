package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.Set;

@CardRegistration(set = "M12", collectorNumber = "1")
public class AegisAngel extends Card {

    public AegisAngel() {
        // When this creature enters, another target permanent gains indestructible for as long
        // as you control this creature.
        PermanentPredicate another = new PermanentNotPredicate(new PermanentIsSourceCardPredicate());
        target(new PermanentPredicateTargetFilter(another, "Target must be another permanent"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GrantKeywordEffect(
                        Set.of(Keyword.INDESTRUCTIBLE), GrantScope.TARGET, another,
                        GrantDuration.WHILE_SOURCE_ON_BATTLEFIELD, null));
    }
}
