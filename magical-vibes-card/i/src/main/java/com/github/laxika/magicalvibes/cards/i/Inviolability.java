package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToRecipientsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MMQ", collectorNumber = "23")
public class Inviolability extends Card {

    public Inviolability() {
        target(TargetFilters.creature())
                .addStaticEffect(PreventDamageToRecipientsEffect.to(Subjects.attachedCreature()));
    }
}
