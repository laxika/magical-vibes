package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "24")
public class TakenosCavalry extends Card {

    public TakenosCavalry() {
        // Bushido 1
        addEffect(EffectSlot.ON_BLOCK, new BushidoEffect(1));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BushidoEffect(1));

        // {T}: This creature deals 1 damage to target attacking or blocking Spirit.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToTargetCreatureEffect(1)),
                "{T}: This creature deals 1 damage to target attacking or blocking Spirit.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.SPIRIT),
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsAttackingPredicate(),
                                        new PermanentIsBlockingPredicate())))),
                        "Target must be an attacking or blocking Spirit")
        ));
    }
}
