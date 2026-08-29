package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "5DN", collectorNumber = "122")
public class Ferropede extends Card {

    public Ferropede() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());

        target(TargetFilters.permanent()).addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(new RemoveCounterFromTargetPermanentEffect(),
                        "Remove a counter from target permanent?"));
    }
}
