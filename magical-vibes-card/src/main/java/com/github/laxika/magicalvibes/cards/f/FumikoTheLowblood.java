package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "BOK", collectorNumber = "104")
public class FumikoTheLowblood extends Card {

    public FumikoTheLowblood() {
        // Bushido X, where X is the number of attacking creatures (counted as the trigger resolves).
        var attackingCreatures = new PermanentCount(new PermanentIsAttackingPredicate(), CountScope.ANY_PLAYER);
        addEffect(EffectSlot.ON_BLOCK, new BushidoEffect(attackingCreatures));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BushidoEffect(attackingCreatures));

        // Creatures your opponents control attack each combat if able.
        addEffect(EffectSlot.STATIC, new MatchingCreaturesMustAttackEffect(
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }
}
