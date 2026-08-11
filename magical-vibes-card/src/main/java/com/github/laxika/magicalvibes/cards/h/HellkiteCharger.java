package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "ZEN", collectorNumber = "131")
public class HellkiteCharger extends Card {

    public HellkiteCharger() {
        addEffect(EffectSlot.ON_ATTACK, new MayPayManaEffect("{5}{R}{R}",
                SequenceEffect.of(
                        new UntapPermanentsEffect(TapUntapScope.ALL_CREATURES,
                                new PermanentIsAttackingPredicate()),
                        new AdditionalCombatPhaseEffect(1)),
                "Pay {5}{R}{R} to untap all attacking creatures and get an additional combat phase?"));
    }
}
