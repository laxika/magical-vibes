package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GoadSourceCreatureWhileControllerControlsItEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "WHO", collectorNumber = "74")
@CardRegistration(set = "WHO", collectorNumber = "679")
@CardRegistration(set = "WHO", collectorNumber = "377")
@CardRegistration(set = "WHO", collectorNumber = "968")
public class VislorTurlough extends Card {

    public VislorTurlough() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                SequenceEffect.of(
                        new ChooseOpponentGainsControlOfSourceEffect(),
                        new GoadSourceCreatureWhileControllerControlsItEffect()),
                "Have an opponent gain control of Vislor Turlough?"));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, SequenceEffect.of(
                new DrawCardEffect(),
                new LoseLifeEffect(new CardsInHand(CountScope.CONTROLLER), LoseLifeRecipient.CONTROLLER)));
    }
}
