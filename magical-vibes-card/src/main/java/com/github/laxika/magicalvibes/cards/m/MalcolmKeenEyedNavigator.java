package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "SPG", collectorNumber = "2")
public class MalcolmKeenEyedNavigator extends Card {

    public MalcolmKeenEyedNavigator() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_DEAL_DAMAGE_TO_OPPONENT,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.PIRATE),
                        CreateTokenEffect.ofTreasureToken(1)));
    }
}
