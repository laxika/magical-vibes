package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalDamageToOpponentsFromColorSourcesEffect;

@CardRegistration(set = "ELD", collectorNumber = "147")
@CardRegistration(set = "SLD", collectorNumber = "463")
@CardRegistration(set = "SLD", collectorNumber = "678")
public class TorbranThaneOfRedFell extends Card {

    public TorbranThaneOfRedFell() {
        addEffect(EffectSlot.STATIC,
                new AdditionalDamageToOpponentsFromColorSourcesEffect(2, CardColor.RED));
    }
}
