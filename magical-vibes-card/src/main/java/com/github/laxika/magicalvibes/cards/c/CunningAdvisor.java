package com.github.laxika.magicalvibes.cards.c;

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

@CardRegistration(set = "PTK", collectorNumber = "72")
public class CunningAdvisor extends Card {

    public CunningAdvisor() {
        // {T}: Target opponent discards a card. Activate only during your turn, before attackers are declared.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER)),
                "{T}: Target opponent discards a card. Activate only during your turn, before attackers are declared.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"),
                null,
                null,
                ActivationTimingRestriction.ONLY_BEFORE_ATTACKERS_DECLARED));
    }
}
