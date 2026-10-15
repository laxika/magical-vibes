package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantGraveyardAbilityToTargetCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;

@CardRegistration(set = "YSOS", collectorNumber = "8")
public class ConsumedByHistory extends Card {

    public ConsumedByHistory() {
        addEffect(EffectSlot.SPELL, new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES,
                new PerpetuallyGrantGraveyardAbilityToTargetCardEffect(Card.unearthAbility("{5}"))));
        addEffect(EffectSlot.SPELL, new MassDamageEffect(3));
    }
}
