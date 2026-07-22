package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExchangeLifeTotalWithCreatureStatEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "135")
public class TreeOfPerdition extends Card {

    public TreeOfPerdition() {
        // {T}: Exchange target opponent's life total with this creature's toughness.
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new ExchangeLifeTotalWithCreatureStatEffect(
                        ExchangeLifeTotalWithCreatureStatEffect.Stat.TOUGHNESS,
                        ExchangeLifeTotalWithCreatureStatEffect.Recipient.TARGET_PLAYER)),
                "{T}: Exchange target opponent's life total with this creature's toughness.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent")));
    }
}
