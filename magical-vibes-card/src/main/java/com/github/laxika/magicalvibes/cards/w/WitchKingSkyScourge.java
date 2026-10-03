package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsForAttackingCreaturesMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LTC", collectorNumber = "511")
@CardRegistration(set = "LTC", collectorNumber = "555")
public class WitchKingSkyScourge extends Card {

    public WitchKingSkyScourge() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new ExileTopCardsForAttackingCreaturesMayPlayThisTurnEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.WRAITH)));
    }
}
