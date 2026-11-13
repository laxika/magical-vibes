package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "36")
public class DrownerInitiate extends Card {

    public DrownerInitiate() {
        // Whenever a player casts a blue spell, you may pay {1}. If you do, target player mills two cards.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addTrigger(Triggers.playerCastsSpell().withFilter(new CardColorPredicate(CardColor.BLUE)),
                new MayPayManaEffect("{1}", new MillEffect(2, MillRecipient.TARGET_PLAYER), "Pay {1}?"));
    }
}
