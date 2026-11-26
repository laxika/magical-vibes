package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "155")
@CardRegistration(set = "EMN", collectorNumber = "128")
@CardRegistration(set = "SIR", collectorNumber = "156")
public class FurybladeVampire extends Card {

    public FurybladeVampire() {
        // At the beginning of combat on your turn, you may discard a card. If you do, this creature
        // gets +3/+0 until end of turn.
        // (Trample is loaded from Scryfall keywords.)
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new MayEffect(
                SequenceEffect.of(
                        new DiscardEffect(1, Subjects.you()),
                        new ConditionalEffect(new HappenedThisWay(StepKind.DISCARD), BoostEffect.self(3, 0))),
                "Discard a card to give this creature +3/+0 until end of turn?"
        ));
    }
}
