package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHistoricPredicate;

/**
 * Traverse Eternity — draw cards equal to the greatest mana value among historic permanents
 * you control.
 */
@CardRegistration(set = "WHO", collectorNumber = "60")
@CardRegistration(set = "WHO", collectorNumber = "665")
@CardRegistration(set = "WHO", collectorNumber = "961")
@CardRegistration(set = "WHO", collectorNumber = "370")
public class TraverseEternity extends Card {

    public TraverseEternity() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(
                new GreatestManaValueAmongControlled(new PermanentIsHistoricPredicate())));
    }
}
