package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExactLifeLoseGameThenAdjustLifeEffect;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "136")
public class Triskaidekaphobia extends Card {

    public Triskaidekaphobia() {
        // "At the beginning of your upkeep, choose one —
        //  • Each player with exactly 13 life loses the game, then each player gains 1 life.
        //  • Each player with exactly 13 life loses the game, then each player loses 1 life."
        // Modal triggered ability: mode picked as the ability resolves (ChooseOneEffectHandler).
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Each player with exactly 13 life loses the game, then each player gains 1 life.",
                        new ExactLifeLoseGameThenAdjustLifeEffect(13, 1)),
                new ChooseOneEffect.ChooseOneOption(
                        "Each player with exactly 13 life loses the game, then each player loses 1 life.",
                        new ExactLifeLoseGameThenAdjustLifeEffect(13, -1))
        )));
    }
}
