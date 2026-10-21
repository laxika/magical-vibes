package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLA", collectorNumber = "158")
public class TyLeeArtfulAcrobat extends Card {

    public TyLeeArtfulAcrobat() {
        // Whenever Ty Lee attacks, you may pay {1}. When you do, target creature can't block this turn.
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ATTACK,
                new MayPayManaEffect("{1}",
                        new QueueReflexiveAbilityEffect(new CantBlockThisTurnEffect(TapUntapScope.TARGET)),
                        "Pay {1} to make target creature unable to block this turn?"));
    }
}
