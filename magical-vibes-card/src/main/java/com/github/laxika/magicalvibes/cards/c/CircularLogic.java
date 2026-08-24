package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

@CardRegistration(set = "TOR", collectorNumber = "33")
public class CircularLogic extends Card {

    public CircularLogic() {
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(
                new CardsInGraveyard(null, CountScope.CONTROLLER)));
        addCastingOption(new MadnessCast("{U}"));
    }
}
