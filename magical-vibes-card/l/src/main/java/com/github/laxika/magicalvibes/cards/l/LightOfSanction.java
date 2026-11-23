package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToRecipientsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "RAV", collectorNumber = "24")
public class LightOfSanction extends Card {

    public LightOfSanction() {
        addStaticEffect(PreventDamageToRecipientsEffect.to(Subjects.creaturesYouControl())
                .fromSourcesYouControl());
    }
}
