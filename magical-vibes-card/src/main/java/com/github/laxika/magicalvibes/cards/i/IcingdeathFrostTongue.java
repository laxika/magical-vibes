package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1018")
@CardRegistration(set = "SLD", collectorNumber = "1019")
public class IcingdeathFrostTongue extends Card {

    public IcingdeathFrostTongue() {
        // Equipped creature gets +2/+0.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE));

        // Whenever equipped creature attacks, tap target creature defending player controls.
        PermanentPredicate defendingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledByDefendingPlayerPredicate()
        ));
        target(new PermanentPredicateTargetFilter(
                defendingCreature,
                "Target must be a creature defending player controls"
        )).addEffect(EffectSlot.ON_ATTACK,
                new TapPermanentsEffect(TapUntapScope.TARGET, defendingCreature));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
