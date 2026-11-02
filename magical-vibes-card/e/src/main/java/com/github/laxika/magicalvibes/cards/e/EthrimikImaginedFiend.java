package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDSK", collectorNumber = "1")
public class EthrimikImaginedFiend extends Card {

    public EthrimikImaginedFiend() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), ManifestDreadEffect.forController());
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES));
        // Ethrimik can't attack or block as long as you control another creature.
        addStaticEffect(new CantAttackOrBlockUnlessEffect(
                new NotCondition(new ControlsAnotherPermanent(new PermanentIsCreaturePredicate())),
                "you control no other creatures"));
    }
}
