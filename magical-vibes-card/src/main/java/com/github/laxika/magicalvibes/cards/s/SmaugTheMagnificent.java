package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "HOB", collectorNumber = "110")
public class SmaugTheMagnificent extends Card {

    public SmaugTheMagnificent() {
        addEffect(EffectSlot.ON_ATTACK, new DealDamageToAnyTargetEffect(new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.TREASURE), CountScope.CONTROLLER)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, CreateTokenEffect.ofTreasureToken(1));
    }
}
