package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayPutCardFromHandOnBottomOfLibraryThenDrawEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "IKO", collectorNumber = "116")
public class FireProphecy extends Card {

    public FireProphecy() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(3))
                .addEffect(EffectSlot.SPELL, new MayPutCardFromHandOnBottomOfLibraryThenDrawEffect());
    }
}
