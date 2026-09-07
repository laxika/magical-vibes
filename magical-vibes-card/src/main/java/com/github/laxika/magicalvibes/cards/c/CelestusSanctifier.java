package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeDayAsEntersEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "MID", collectorNumber = "12")
public class CelestusSanctifier extends Card {

    public CelestusSanctifier() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeDayAsEntersEffect());
        addEffect(EffectSlot.ON_DAY_NIGHT_CHANGE,
                LookAtTopCardsEffect.putOneIntoGraveyardRestOnTop(2));
    }
}
