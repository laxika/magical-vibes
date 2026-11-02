package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "96")
@CardRegistration(set = "INR", collectorNumber = "371")
@CardRegistration(set = "SOI", collectorNumber = "99")
@CardRegistration(set = "C19", collectorNumber = "103")
public class AsylumVisitor extends Card {

    public AsylumVisitor() {
        // Madness {1}{B}
        addCastingOption(new MadnessCast("{1}{B}"));

        // At the beginning of each player's upkeep, if that player has no cards in hand,
        // you draw a card and you lose 1 life.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ConditionalEffect(
                new ActivePlayerHandEmpty(),
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1))));
        addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT), new ConditionalEffect(
                new ActivePlayerHandEmpty(),
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1))));
    }
}
