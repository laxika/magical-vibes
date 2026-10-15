package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.SourcePowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "2")
@CardRegistration(set = "FIC", collectorNumber = "168")
@CardRegistration(set = "FIC", collectorNumber = "202")
@CardRegistration(set = "FIC", collectorNumber = "210")
@CardRegistration(set = "FIC", collectorNumber = "221")
public class CloudExSOLDIER extends Card {

    public CloudExSOLDIER() {
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                "Target must be an Equipment you control"), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AttachTargetEquipmentToSourceEffect());

        PermanentAllOfPredicate equippedAttackingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsEquippedPredicate(),
                new PermanentIsAttackingPredicate()));
        PermanentCount equippedAttackingCreatures = new PermanentCount(
                equippedAttackingCreature, CountScope.CONTROLLER);

        addEffect(EffectSlot.ON_ATTACK, new DrawCardEffect(equippedAttackingCreatures));
        addEffect(EffectSlot.ON_ATTACK, ConditionalEffect.unless(
                new SourcePowerAtLeast(7), CreateTokenEffect.ofTreasureToken(2)));
    }
}
