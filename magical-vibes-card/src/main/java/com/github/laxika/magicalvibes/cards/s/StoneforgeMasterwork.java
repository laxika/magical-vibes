package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesCreatureTypeWithEquippedCreaturePredicate;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "166")
public class StoneforgeMasterwork extends Card {

    public StoneforgeMasterwork() {
        PermanentPredicate otherMatchingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentSharesCreatureTypeWithEquippedCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsHostOfSourceAuraPredicate())));
        PermanentCount otherMatchingCreatures = new PermanentCount(otherMatchingCreature, CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                otherMatchingCreatures, otherMatchingCreatures, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
