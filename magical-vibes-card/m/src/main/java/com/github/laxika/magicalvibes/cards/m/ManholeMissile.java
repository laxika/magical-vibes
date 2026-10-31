package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayPutCardFromHandOnBottomOfLibraryThenDrawEffect;

@CardRegistration(set = "TMT", collectorNumber = "94")
public class ManholeMissile extends Card {

    public ManholeMissile() {
        addSpellEffect(new DealDamageToTargetCreatureEffect(3));
        addSpellEffect(new MayPutCardFromHandOnBottomOfLibraryThenDrawEffect());
    }
}
