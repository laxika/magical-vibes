package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaValueParity;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageFromCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.CardManaValueParityPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueParityPredicate;

@CardRegistration(set = "IKO", collectorNumber = "228")
@CardRegistration(set = "MUL", collectorNumber = "54")
@CardRegistration(set = "MUL", collectorNumber = "119")
@CardRegistration(set = "MUL", collectorNumber = "184")
public class OboshThePreypiercer extends Card {

    public OboshThePreypiercer() {
        addStaticEffect(DoubleDamageFromCreaturesEffect.fromSourcesYouControl(
                new PermanentManaValueParityPredicate(ManaValueParity.ODD),
                new CardManaValueParityPredicate(ManaValueParity.ODD)));
    }
}
