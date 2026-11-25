package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "37")
public class ThousandMoonsCrackshot extends Card {

    public ThousandMoonsCrackshot() {
        // Whenever this creature attacks, you may pay {2}{W}. When you do, tap target creature.
        // The reflexive trigger chooses its target only after the payment (CR 603.12).
        addTrigger(Triggers.attacks(), UnlessPaysEffect.youMayPay("{2}{W}", new QueueReflexiveAbilityEffect(
                        new TapPermanentsEffect(Subjects.target(), new PermanentIsCreaturePredicate()))).withPrompt("Pay {2}{W} to tap target creature?"));
    }
}
