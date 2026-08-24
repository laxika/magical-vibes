package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "14")
public class ManaclesOfDecay extends Card {

    public ManaclesOfDecay() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect(true, false));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new BoostReferencedPermanentEffect(PermanentReference.ATTACHED, -1, -1)),
                "{B}: Enchanted creature gets -1/-1 until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new CantBlockThisTurnEffect(TapUntapScope.ENCHANTED)),
                "{R}: Enchanted creature can't block this turn."
        ));
    }
}
