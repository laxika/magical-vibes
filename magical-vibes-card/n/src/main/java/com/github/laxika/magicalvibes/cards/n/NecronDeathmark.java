package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "42")
public class NecronDeathmark extends Card {

    public NecronDeathmark() {
        SpellTarget playerTarget = target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"));
        SpellTarget creatureTarget = target(TargetFilters.creature(), 0, 1);

        var destroyTargetPermanent = new DestroyTargetPermanentEffect();
        creatureTarget.bindEffect(destroyTargetPermanent);
        var mill = new MillEffect(3, MillRecipient.TARGET_PLAYER);
        playerTarget.bindEffect(mill);
        addTrigger(Triggers.enters(ObjectRelation.THIS), destroyTargetPermanent, mill);
    }
}
