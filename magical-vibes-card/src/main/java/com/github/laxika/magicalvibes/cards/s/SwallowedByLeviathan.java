package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "FIN", collectorNumber = "79")
public class SwallowedByLeviathan extends Card {

    public SwallowedByLeviathan() {
        addEffect(EffectSlot.SPELL, new SurveilEffect(2));
        addEffect(EffectSlot.SPELL,
                new CounterUnlessPaysEffect(new CardsInGraveyard(null, CountScope.CONTROLLER)));
    }
}
