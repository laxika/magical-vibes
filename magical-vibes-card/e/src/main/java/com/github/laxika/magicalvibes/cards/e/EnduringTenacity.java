package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DyingPermanentWasCreatureConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "95")
public class EnduringTenacity extends Card {

    public EnduringTenacity() {
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE,
                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.TARGET_PLAYER));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DyingPermanentWasCreatureConditionalEffect(
                new ReturnSourceCardFromGraveyardToBattlefieldEffect(false, Set.of(CardType.ENCHANTMENT))));
    }
}
