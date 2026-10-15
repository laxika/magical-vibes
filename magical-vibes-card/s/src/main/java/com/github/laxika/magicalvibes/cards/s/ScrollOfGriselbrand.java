package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "221")
public class ScrollOfGriselbrand extends Card {

    public ScrollOfGriselbrand() {
        // {1}, Sacrifice Scroll of Griselbrand: Target opponent discards a card.
        // If you control a Demon, that player loses 3 life.
        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(
                        new SacrificeSelfCost(),
                        new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER),
                        new ConditionalEffect(
                                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.DEMON)),
                                new LoseLifeEffect(3, LoseLifeRecipient.TARGET_PLAYER))),
                "{1}, Sacrifice Scroll of Griselbrand: Target opponent discards a card. If you control a Demon, that player loses 3 life.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent")));
    }
}
