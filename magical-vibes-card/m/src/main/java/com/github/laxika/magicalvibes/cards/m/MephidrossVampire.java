package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5DN", collectorNumber = "53")
@CardRegistration(set = "SPG", collectorNumber = "4")
public class MephidrossVampire extends Card {

    public MephidrossVampire() {
        addStaticEffect(new GrantSubtypeEffect(CardSubtype.VAMPIRE, GrantScope.ALL_OWN_CREATURES));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.dealsDamageToCreature(),
                new PutCountersOnSourceEffect(1, 1, 1)),
                GrantScope.ALL_OWN_CREATURES));
    }
}
