package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnOpponentHasMoreCardsInHandThanController;
import com.github.laxika.magicalvibes.model.condition.AnOpponentHasMoreLifeThanController;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsMoreCreatures;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "38")
public class SunsetRevelry extends Card {

    public SunsetRevelry() {
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new AnOpponentHasMoreLifeThanController(), new GainLifeEffect(4)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new OpponentControlsMoreCreatures(1),
                new CreateTokenEffect(2, "Human", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.HUMAN), Set.of(), Set.of())));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new AnOpponentHasMoreCardsInHandThanController(), new DrawCardEffect(1)));
    }
}
