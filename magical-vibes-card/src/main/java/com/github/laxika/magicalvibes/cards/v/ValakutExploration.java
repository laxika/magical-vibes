package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.SourceExiledCardsThreshold;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceToOwnerGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "ZNR", collectorNumber = "175")
public class ValakutExploration extends Card {

    public ValakutExploration() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new ExileTopCardsToSourceEffect(1, false, false));
        addEffect(EffectSlot.STATIC,
                new AllowCastFromCardsExiledWithSourceEffect(
                        false, null, false, false, 0, null, false, false, false, true));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(
                        new SourceExiledCardsThreshold(1),
                        SequenceEffect.of(
                                new ReturnAllCardsExiledWithSourceToOwnerGraveyardEffect(),
                                new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.EACH_OPPONENT))));
    }
}
