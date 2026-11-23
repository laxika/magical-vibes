package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToRecipientsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ARN", collectorNumber = "38")
public class DesertNomads extends Card {

    public DesertNomads() {
        addStaticEffect(PreventDamageToRecipientsEffect.to(Subjects.source())
                .from(new PermanentHasSubtypePredicate(CardSubtype.DESERT)));
    }
}
