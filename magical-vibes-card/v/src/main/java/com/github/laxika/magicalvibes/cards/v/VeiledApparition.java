package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.SourceIsEnchantment;
import com.github.laxika.magicalvibes.model.condition.SourceIsCreature;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "107")
public class VeiledApparition extends Card {

    public VeiledApparition() {
        addTrigger(Triggers.opponentCastsSpell().anySpell(),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsEnchantment()),
                new BecomeCreatureEffect(3, 3, CardSubtype.ILLUSION, Set.of(Keyword.FLYING)));

        addStaticEffect(new ConditionalEffect(
                new SourceIsCreature(),
                new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU),
                        new ForcedCostOrElseEffect(
                                new PayManaCost("{1}{U}"),
                                List.of(new SacrificeSelfEffect()),
                                true)),
                        GrantScope.SELF)));
    }
}
