package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GuildFeudEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "RTR", collectorNumber = "97")
public class GuildFeud extends Card {

    public GuildFeud() {
        // At the beginning of your upkeep, target opponent reveals the top three cards of their
        // library, may put a creature card from among them onto the battlefield, then puts the rest
        // into their graveyard. You do the same with the top three cards of your library. If two
        // creatures are put onto the battlefield this way, those creatures fight each other.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.UPKEEP_TRIGGERED, new GuildFeudEffect());
    }
}
