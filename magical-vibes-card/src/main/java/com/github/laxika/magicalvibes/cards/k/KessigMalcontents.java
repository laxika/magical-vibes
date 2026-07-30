package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "AVR", collectorNumber = "142")
public class KessigMalcontents extends Card {

    public KessigMalcontents() {
        // "When this creature enters, it deals damage to target player or planeswalker equal to
        // the number of Humans you control." The count is read at resolution, so this creature
        // (a Human) counts itself. The planeswalker filter narrows the permanent side of
        // "player or planeswalker"; players are always legal.
        target(new PermanentPredicateTargetFilter(new PermanentIsPlaneswalkerPredicate(),
                "Target must be a player or planeswalker"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(
                                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.HUMAN),
                                        CountScope.CONTROLLER)));
    }
}
