package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureFromGraveyardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "ECL", collectorNumber = "122")
public class TwilightDiviner extends Card {

    public TwilightDiviner() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SurveilEffect(2));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureFromGraveyardConditionalEffect(
                        new OncePerTurnTriggerEffect(new CreateTokenCopyOfTargetPermanentEffect())));
    }
}
