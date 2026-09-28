package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DawnbreakReclaimerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "SLD", collectorNumber = "1346")
@CardRegistration(set = "C15", collectorNumber = "2")
public class DawnbreakReclaimer extends Card {

    public DawnbreakReclaimer() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new DawnbreakReclaimerEffect(new CardTypePredicate(CardType.CREATURE)));
    }
}
