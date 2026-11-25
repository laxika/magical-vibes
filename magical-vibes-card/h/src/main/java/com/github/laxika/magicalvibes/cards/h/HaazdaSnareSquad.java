package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DGM", collectorNumber = "2")
public class HaazdaSnareSquad extends Card {

    public HaazdaSnareSquad() {
        // Whenever this creature attacks, you may pay {W}. If you do, tap target creature an
        // opponent controls. Target is chosen as the trigger goes on the stack (CR 603.3d);
        // the payment choice happens at resolution (CR 603.5).
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.attacks(), UnlessPaysEffect.youMayPay("{W}", new TapPermanentsEffect(Subjects.target()))
                        .withPrompt("Pay {W} to tap target creature?"));
    }
}
