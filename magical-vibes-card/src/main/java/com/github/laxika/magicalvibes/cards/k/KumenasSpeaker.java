package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlsAnotherSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "XLN", collectorNumber = "196")
public class KumenasSpeaker extends Card {

    public KumenasSpeaker() {
        // This creature gets +1/+1 as long as you control another Merfolk or an Island.
        addEffect(EffectSlot.STATIC, new ControlsAnotherSubtypeConditionalEffect(
                Set.of(CardSubtype.MERFOLK, CardSubtype.ISLAND),
                false,
                new StaticBoostEffect(1, 1, GrantScope.SELF)
        ));
    }
}
