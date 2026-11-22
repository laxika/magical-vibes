package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "APC", collectorNumber = "52")
@CardRegistration(set = "PHUK", collectorNumber = "26")
public class Suppress extends Card {

    public Suppress() {
        addSpellEffect(new ExileTargetPlayerHandEffect().exiledFaceDown());
        addSpellEffect(RegisterDelayedStepTriggerEffect.aboutExiled(Triggers.endStep(ActivePlayer.CHOSEN_PLAYER),
                        new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.EXILE)))
                .ofThatPlayersNextTurn(Subjects.targetPlayerDeclaredElsewhere()));
    }
}
