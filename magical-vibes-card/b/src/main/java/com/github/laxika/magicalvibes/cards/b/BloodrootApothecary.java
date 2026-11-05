package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BLC", collectorNumber = "27")
@CardRegistration(set = "BLC", collectorNumber = "61")
public class BloodrootApothecary extends Card {

    public BloodrootApothecary() {
        var createTokenForTargetPlayer = new CreateTokenForTargetPlayerEffect(CreateTokenEffect.ofTreasureToken(1));
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).bindEffect(createTokenForTargetPlayer);
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                createTokenForTargetPlayer,
                CreateTokenEffect.ofTreasureToken(1));

        addTrigger(Triggers.sacrificed(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsTokenPredicate(),
                                new PermanentNotPredicate(new PermanentIsCreaturePredicate()))),
                        ObjectRelation.OPPONENT_CONTROLS),
                new GivePoisonCountersEffect(2, PoisonRecipient.TARGET_PLAYER));
    }
}
