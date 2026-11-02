package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TOR", collectorNumber = "50")
public class BoneshardSlasher extends Card {

    public BoneshardSlasher() {
        addStaticEffect(new ConditionalEffect(
                new GraveyardCardThreshold(7, null),
                new StaticBoostEffect(2, 2, GrantScope.SELF)));
        addStaticEffect(new ConditionalEffect(
                new GraveyardCardThreshold(7, null),
                new GrantTriggeredAbilityEffect(new TriggeredAbility(Triggers.becomesTargetOfSpellOrAbility(),
                        new SacrificeSelfEffect()), GrantScope.SELF)));
    }
}
