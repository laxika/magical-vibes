package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseNameRevealRandomCardsFromTargetHandDiscardMatchingEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "80")
public class Nebuchadnezzar extends Card {

    public Nebuchadnezzar() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(new ChooseNameRevealRandomCardsFromTargetHandDiscardMatchingEffect(new XValue())),
                "{X}, {T}: Choose a card name. Target opponent reveals X cards at random from their hand. "
                        + "Then that player discards all cards with that name revealed this way. Activate only during your turn.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"),
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN));
    }
}
