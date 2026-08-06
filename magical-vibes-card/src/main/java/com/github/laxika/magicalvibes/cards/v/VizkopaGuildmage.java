package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterLifeGainOpponentLifeLossThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import java.util.List;

/**
 * Vizkopa Guildmage — the first ability is a plain targeted lifelink grant; the second creates a
 * turn-scoped delayed trigger that drains each opponent for every life-gain event the controller
 * has for the rest of the turn.
 */
@CardRegistration(set = "GTC", collectorNumber = "206")
public class VizkopaGuildmage extends Card {

    public VizkopaGuildmage() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{W}{B}",
                List.of(new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.TARGET)),
                "{1}{W}{B}: Target creature gains lifelink until end of turn.",
                new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(),
                        "Target must be a creature")));

        addActivatedAbility(new ActivatedAbility(false, "{1}{W}{B}",
                List.of(new RegisterLifeGainOpponentLifeLossThisTurnEffect()),
                "{1}{W}{B}: Whenever you gain life this turn, each opponent loses that much life."));
    }
}
