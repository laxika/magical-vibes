package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;

@CardRegistration(set = "KTK", collectorNumber = "173")
@CardRegistration(set = "SOA", collectorNumber = "63")
@CardRegistration(set = "MKC", collectorNumber = "205")
public class DeflectingPalm extends Card {

    public DeflectingPalm() {
        addSpellEffect(PreventDamageFromChosenSourceEffect.nextDamageToYouAndDamageSourceController());
    }
}
