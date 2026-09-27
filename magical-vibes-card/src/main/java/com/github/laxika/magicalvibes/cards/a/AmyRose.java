package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2083")
public class AmyRose extends Card {

    public AmyRose() {
        PermanentPredicate otherAttackingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsAttackingPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));

        target(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                "Target must be an Equipment"), 0, 1)
                .addEffect(EffectSlot.ON_ATTACK, new AttachTargetEquipmentToTriggeringPermanentEffect());

        target(new PermanentPredicateTargetFilter(otherAttackingCreature,
                "Target must be another attacking creature"), 0, 1)
                .addEffect(EffectSlot.ON_ATTACK,
                        new BoostTargetCreatureEffect(new SourcePower(), new Fixed(0)));
    }
}
