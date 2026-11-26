package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ULG", collectorNumber = "129")
@CardRegistration(set = "V10", collectorNumber = "8")
@CardRegistration(set = "VMA", collectorNumber = "276")
@CardRegistration(set = "AA3", collectorNumber = "22")
public class MemoryJar extends Card {

    public MemoryJar() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        ExileTargetPlayerHandEffect.handsOf(Subjects.eachPlayer()).exiledFaceDown(),
                        RegisterDelayedStepTriggerEffect.aboutExiled(Triggers.endStep(ActivePlayer.ANY),
                                        new DiscardHandEffect(Subjects.eachPlayer()),
                                        new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.EXILE)))
                                .evenIfNothingHappened(),
                        new DrawCardEffect(7, Subjects.eachPlayer())
                ),
                "{T}, Sacrifice Memory Jar: Each player exiles all cards from their hand face down and draws seven cards. At the beginning of the next end step, each player discards their hand and returns to their hand each card they exiled this way."
        ));
    }
}
