package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameFromLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardsEqualToLifeGainedEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "DSK", collectorNumber = "64")
public class MarinaVendrellsGrimoire extends Card {

    public MarinaVendrellsGrimoire() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new WasCast(), new DrawCardEffect(5)));

        addEffect(EffectSlot.STATIC, new NoMaximumHandSizeEffect());
        addEffect(EffectSlot.STATIC, new CantLoseGameFromLifeEffect());

        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new DrawCardsEqualToLifeGainedEffect());
        addEffect(EffectSlot.ON_CONTROLLER_LOSES_LIFE, SequenceEffect.of(
                new DiscardEffect(new EventValue(), DiscardRecipient.CONTROLLER),
                new ConditionalEffect(new ControllerHandEmpty(), new ControllerLosesGameEffect())));
    }
}
