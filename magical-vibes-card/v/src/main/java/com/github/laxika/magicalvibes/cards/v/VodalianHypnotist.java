package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "84")
public class VodalianHypnotist extends Card {

    public VodalianHypnotist() {
        addActivatedAbility(new ActivatedAbility(
                true, "{2}{B}",
                List.of(new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER)),
                "{2}{B}, {T}: Target player discards a card. Activate only as a sorcery.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                ),
                null, null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
