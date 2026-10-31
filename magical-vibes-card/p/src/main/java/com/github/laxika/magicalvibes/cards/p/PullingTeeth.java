package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.WonClash;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "MOR", collectorNumber = "75")
public class PullingTeeth extends Card {

    public PullingTeeth() {
        // Clash with an opponent. If you win, target player discards two cards (one discard event).
        // Otherwise, that player discards a card: the win-only discard is the clash reward, and the
        // lost-clash discard is gated on not having won the clash.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"))
                .addSpellEffect(new ClashEffect(new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER)))
                .addSpellEffect(new ConditionalEffect(new NotCondition(new WonClash()),
                        new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER)));
    }
}
