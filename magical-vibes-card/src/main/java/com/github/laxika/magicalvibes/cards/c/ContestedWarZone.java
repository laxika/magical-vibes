package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGainsControlOfThisPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "144")
public class ContestedWarZone extends Card {

    public ContestedWarZone() {
        // Whenever a creature deals combat damage to you, that creature's controller gains control of Contested War Zone.
        addEffect(EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU,
                new DamageSourceControllerGainsControlOfThisPermanentEffect(true, true));

        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));

        // {1}, {T}: Attacking creatures get +1/+0 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new BoostAllCreaturesEffect(1, 0, new PermanentIsAttackingPredicate())),
                "{1}, {T}: Attacking creatures get +1/+0 until end of turn."
        ));
    }
}
