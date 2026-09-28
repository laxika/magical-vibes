package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "31")
@CardRegistration(set = "FIC", collectorNumber = "139")
public class ThancredWaters extends Card {

    public ThancredWaters() {
        PermanentPredicate anotherLegendaryPermanent = new PermanentAllOfPredicate(List.of(
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));

        target(new ControlledPermanentPredicateTargetFilter(
                anotherLegendaryPermanent,
                "Target must be another legendary permanent you control"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GrantKeywordEffect(
                Set.of(Keyword.INDESTRUCTIBLE),
                GrantScope.TARGET,
                anotherLegendaryPermanent,
                GrantDuration.WHILE_SOURCE_ON_BATTLEFIELD,
                null
        ));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF))
        ));
    }
}
