package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "INR", collectorNumber = "96")
public class AsylumVisitor extends Card {

    public AsylumVisitor() {
        // Madness {1}{B}
        addCastingOption(new MadnessCast("{1}{B}"));

        // At the beginning of each player's upkeep, if that player has no cards in hand,
        // you draw a card and you lose 1 life.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new ActivePlayerHandEmpty(),
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1))));
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED, new ConditionalEffect(
                new ActivePlayerHandEmpty(),
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1))));
    }
}
