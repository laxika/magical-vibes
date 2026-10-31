package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageFromOpponentSourcesToControllerAndPlaneswalkersEffect;

@CardRegistration(set = "MAR", collectorNumber = "1")
@CardRegistration(set = "OMB", collectorNumber = "1")
@CardRegistration(set = "C14", collectorNumber = "4")
@CardRegistration(set = "MKC", collectorNumber = "58")
public class Comeuppance extends Card {

    public Comeuppance() {
        addSpellEffect(new PreventAllDamageFromOpponentSourcesToControllerAndPlaneswalkersEffect());
    }
}
