package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageFromEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "146")
public class MjLnirHammerOfThor extends Card {

    private static final PermanentPredicate WORTHY_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
            new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.VILLAIN)),
            new PermanentAnyOfPredicate(List.of(
                    new PermanentColorInPredicate(Set.of(CardColor.RED)),
                    new PermanentColorInPredicate(Set.of(CardColor.WHITE))
            ))
    ));

    public MjLnirHammerOfThor() {
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToTargetCreatureEffect(4));
        addEffect(EffectSlot.STATIC, new DoubleDamageFromEquippedCreatureEffect());
        addActivatedAbility(new EquipActivatedAbility("{1}", WORTHY_CREATURE,
                "Target must be a worthy creature"));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new MassDamageEffect(2)),
                "{2}{R}, Discard this card: It deals 2 damage to each creature."
        ));
    }
}
