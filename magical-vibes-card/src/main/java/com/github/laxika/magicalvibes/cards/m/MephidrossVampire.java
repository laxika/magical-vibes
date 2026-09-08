package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "5DN", collectorNumber = "53")
public class MephidrossVampire extends Card {

    public MephidrossVampire() {
        addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.VAMPIRE, GrantScope.ALL_OWN_CREATURES));
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE,
                new PutCountersOnSourceEffect(1, 1, 1),
                GrantScope.ALL_OWN_CREATURES));
    }
}
