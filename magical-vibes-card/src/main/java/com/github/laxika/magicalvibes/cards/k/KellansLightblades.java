package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "18")
public class KellansLightblades extends Card {

    public KellansLightblades() {
        PermanentPredicate attackingOrBlockingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentIsBlockingPredicate()
                ))
        ));

        addEffect(EffectSlot.STATIC, new KickerEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate(),
                        new PermanentIsTokenPredicate()
                )),
                "an artifact, enchantment, or token"
        ));
        target(new PermanentPredicateTargetFilter(
                attackingOrBlockingCreature,
                "Target must be an attacking or blocking creature"
        )).addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Kicked(),
                new DealDamageToTargetCreatureEffect(3, attackingOrBlockingCreature),
                new DestroyTargetPermanentEffect(attackingOrBlockingCreature)
        ));
    }
}
