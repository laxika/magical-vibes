package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;


@CardRegistration(set = "AKH", collectorNumber = "249")
@CardRegistration(set = "AKR", collectorNumber = "331")
public class SunscorchedDesert extends Card {

    public SunscorchedDesert() {
        // "When this land enters, it deals 1 damage to target player or planeswalker."
        // Lands are played, never cast, so the mandatory ETB target is chosen as the ability goes
        // on the stack; the planeswalker filter narrows the permanent side of "player or
        // planeswalker" (same idiom as Noggle Hedge-Mage).
        target(new AnyTargetPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player or planeswalker"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToTargetPlayerOrPlaneswalkerEffect(1));

        // "{T}: Add {C}."
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
