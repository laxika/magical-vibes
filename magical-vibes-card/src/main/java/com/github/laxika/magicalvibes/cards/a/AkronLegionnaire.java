package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesCantAttackUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "2")
public class AkronLegionnaire extends Card {

    public AkronLegionnaire() {
        // Except for creatures named Akron Legionnaire and artifact creatures,
        // creatures you control can't attack.
        addEffect(EffectSlot.STATIC, new ControlledCreaturesCantAttackUnlessPredicateEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentNamedPredicate("Akron Legionnaire"),
                        new PermanentIsArtifactPredicate()))));
    }
}
