package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MAT", collectorNumber = "31")
@CardRegistration(set = "MAT", collectorNumber = "81")
@CardRegistration(set = "MAT", collectorNumber = "131")
@CardRegistration(set = "MAT", collectorNumber = "211")
public class GoldForgedThopteryx extends Card {

    public GoldForgedThopteryx() {
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                new CounterUnlessPaysEffect(2)),
                GrantScope.OWN_PERMANENTS,
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)));
    }
}
