package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GhastlyHaunting;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfAndAttachToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByTriggeringPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "50")
public class SoulSeizer extends Card {

    public SoulSeizer() {
        setBackFaceCard(new GhastlyHaunting());

        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(new TransformSelfAndAttachToTargetCreatureEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledByTriggeringPlayerPredicate()))),
                        "Transform Soul Seizer into Ghastly Haunting and attach it to a creature that player controls?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "GhastlyHaunting";
    }
}
