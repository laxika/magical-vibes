package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "NEO", collectorNumber = "144")
public class GoShintaiOfAncientWars extends Card {

    public GoShintaiOfAncientWars() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                MayPayManaEffect.reflexiveTarget("{1}",
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(
                                new PermanentCount(
                                        new PermanentHasSubtypePredicate(CardSubtype.SHRINE),
                                        CountScope.CONTROLLER)),
                        "Pay {1} to deal damage to target player or planeswalker?"));
    }
}
