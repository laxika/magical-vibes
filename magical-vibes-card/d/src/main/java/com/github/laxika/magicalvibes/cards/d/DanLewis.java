package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "78")
@CardRegistration(set = "WHO", collectorNumber = "380")
@CardRegistration(set = "WHO", collectorNumber = "683")
@CardRegistration(set = "WHO", collectorNumber = "971")
public class DanLewis extends Card {

    public DanLewis() {
        PermanentPredicate noncreatureNonEquipmentArtifact = new PermanentAllOfPredicate(List.of(
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentIsCreaturePredicate()),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT))
        ));

        addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(
                CardSubtype.EQUIPMENT, GrantScope.ALL_PERMANENTS, false, noncreatureNonEquipmentArtifact));
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new StaticBoostEffect(1, 0, GrantScope.EQUIPPED_CREATURE),
                GrantScope.ALL_PERMANENTS, noncreatureNonEquipmentArtifact));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new EquipActivatedAbility("{1}"), GrantScope.ALL_PERMANENTS, noncreatureNonEquipmentArtifact));
    }
}
