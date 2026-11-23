package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

/**
 * Vizkopa Guildmage — the first ability is a plain targeted lifelink grant; the second creates a
 * turn-scoped delayed trigger that drains each opponent for every life-gain event the controller
 * has for the rest of the turn.
 */
@CardRegistration(set = "GTC", collectorNumber = "206")
@CardRegistration(set = "GK2", collectorNumber = "47")
@CardRegistration(set = "IMA", collectorNumber = "211")
@CardRegistration(set = "C13", collectorNumber = "225")
public class VizkopaGuildmage extends Card {

    public VizkopaGuildmage() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{W}{B}",
                List.of(new GrantKeywordEffect(Keyword.LIFELINK, Subjects.target())),
                "{1}{W}{B}: Target creature gains lifelink until end of turn.",
                new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(),
                        "Target must be a creature")));

        addActivatedAbility(new ActivatedAbility(false, "{1}{W}{B}",
                List.of(new RegisterGlobalTriggeredAbilityEffect(new TriggeredAbility(
                        Triggers.youGainLife(),
                        new LoseLifeEffect(new EventValue(), Subjects.eachOpponent())))),
                "{1}{W}{B}: Whenever you gain life this turn, each opponent loses that much life."));
    }
}
