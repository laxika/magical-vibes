package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OHOP", collectorNumber = "1")
@CardRegistration(set = "OPCA", collectorNumber = "9")
public class AcademyAtTolariaWest extends Card {

    public AcademyAtTolariaWest() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new ConditionalEffect(new ControllerHandEmpty(), new DrawCardEffect(7)));
        addEffect(EffectSlot.CHAOS_TRIGGERED, new DiscardHandEffect());
    }
}
