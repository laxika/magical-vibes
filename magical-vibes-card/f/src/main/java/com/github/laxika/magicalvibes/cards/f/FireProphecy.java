package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayPutCardFromHandOnBottomOfLibraryThenDrawEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "IKO", collectorNumber = "116")
public class FireProphecy extends Card {

    public FireProphecy() {
        target(TargetFilters.creature()).addSpellEffect(new DealDamageToTargetCreatureEffect(3))
                .addSpellEffect(new MayPutCardFromHandOnBottomOfLibraryThenDrawEffect());
    }
}
