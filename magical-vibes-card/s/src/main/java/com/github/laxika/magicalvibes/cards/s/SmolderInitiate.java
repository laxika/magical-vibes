package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "78")
public class SmolderInitiate extends Card {

    public SmolderInitiate() {
        // Whenever a player casts a black spell, you may pay {1}. If you do, target player loses 1 life.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addTrigger(Triggers.playerCastsSpell().withFilter(new CardColorPredicate(CardColor.BLACK)),
                new MayPayManaEffect("{1}", new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER), "Pay {1} to make target player lose 1 life?"));
    }
}
