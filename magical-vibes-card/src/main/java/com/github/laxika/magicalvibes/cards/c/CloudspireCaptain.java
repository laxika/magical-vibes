package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PowerBoostForCrewAndSaddleEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "9")
public class CloudspireCaptain extends Card {

    public CloudspireCaptain() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                1, 1, GrantScope.OWN_PERMANENTS,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.MOUNT, CardSubtype.VEHICLE))));
        addEffect(EffectSlot.STATIC, new PowerBoostForCrewAndSaddleEffect(2));
    }
}
