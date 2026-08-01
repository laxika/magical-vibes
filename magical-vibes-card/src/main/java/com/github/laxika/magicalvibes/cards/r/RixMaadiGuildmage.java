package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerLostLifeThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "192")
public class RixMaadiGuildmage extends Card {

    public RixMaadiGuildmage() {
        // {B}{R}: Target blocking creature gets -1/-1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}{R}",
                List.of(new BoostTargetCreatureEffect(-1, -1)),
                "{B}{R}: Target blocking creature gets -1/-1 until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsBlockingPredicate(),
                        "Target must be a blocking creature"
                )
        ));

        // {B}{R}: Target player who lost life this turn loses 1 life.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}{R}",
                List.of(new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER)),
                "{B}{R}: Target player who lost life this turn loses 1 life.",
                new PlayerPredicateTargetFilter(
                        new PlayerLostLifeThisTurnPredicate(),
                        "Target must be a player who lost life this turn"
                )
        ));
    }
}
