package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;

@CardRegistration(set = "FUT", collectorNumber = "8")
public class InterventionPact extends Card {

    public InterventionPact() {
        addSpellEffect(PreventDamageFromChosenSourceEffect.nextDamageToYouAndGainLife());
        addSpellEffect(RegisterDelayedStepTriggerEffect.atNext(Triggers.upkeep(ActivePlayer.YOU),
                UnlessPaysEffect.unlessYouPay("{1}{W}{W}", new ControllerLosesGameEffect())));
    }
}
