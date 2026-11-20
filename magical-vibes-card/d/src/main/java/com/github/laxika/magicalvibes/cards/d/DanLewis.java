package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
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

        addStaticEffect(new GrantSubtypeEffect(
                CardSubtype.EQUIPMENT, Subjects.otherPermanents(), false, noncreatureNonEquipmentArtifact));
        addStaticEffect(new GrantEffectEffect(
                new StaticBoostEffect(1, 0, Subjects.attachedCreature()),
                Subjects.otherPermanents(), noncreatureNonEquipmentArtifact));
        addStaticEffect(new GrantActivatedAbilityEffect(
                new EquipActivatedAbility("{1}"), Subjects.otherPermanents(), noncreatureNonEquipmentArtifact));
    }
}
