package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIntensityThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.InitializeSourceIntensityEffect;
import com.github.laxika.magicalvibes.model.effect.IntensifyNamedCardsEffect;

import java.util.List;

@CardRegistration(set = "YBRO", collectorNumber = "5")
public class PieceItTogether extends Card {

    private static final String CARD_NAME = "Piece It Together";
    private static final Condition INTENSITY_FOUR = new AllOf(List.of(
            new SourceIntensityThreshold(4),
            new NotCondition(new SourceIntensityThreshold(5))));

    public PieceItTogether() {
        // Starting intensity 1.
        addEffect(EffectSlot.SPELL, new InitializeSourceIntensityEffect(1));

        // Draw a card unless Piece It Together's intensity is exactly 4.
        addEffect(EffectSlot.SPELL,
                new ConditionalEffect(new NotCondition(INTENSITY_FOUR), new DrawCardEffect()));

        // If Piece It Together's intensity is exactly 4, take an extra turn instead.
        addEffect(EffectSlot.SPELL,
                new ConditionalEffect(INTENSITY_FOUR, new ControllerExtraTurnEffect(1)));

        // Cards you own named Piece It Together intensify by 1.
        addEffect(EffectSlot.SPELL, new IntensifyNamedCardsEffect(CARD_NAME));
    }
}
