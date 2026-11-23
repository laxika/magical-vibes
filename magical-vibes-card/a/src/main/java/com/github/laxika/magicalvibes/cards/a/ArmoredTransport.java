package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToRecipientsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockingSourcePredicate;

@CardRegistration(set = "GTC", collectorNumber = "226")
public class ArmoredTransport extends Card {

    public ArmoredTransport() {
        addStaticEffect(PreventDamageToRecipientsEffect.to(Subjects.source())
                .from(new PermanentBlockingSourcePredicate()).combatOnly());
    }
}
