package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerLifeExactlyPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "136")
@CardRegistration(set = "INR", collectorNumber = "391")
@CardRegistration(set = "SOI", collectorNumber = "141")
@CardRegistration(set = "A25", collectorNumber = "110")
@CardRegistration(set = "SIR", collectorNumber = "137")
public class Triskaidekaphobia extends Card {

    public Triskaidekaphobia() {
        // "At the beginning of your upkeep, choose one —
        //  • Each player with exactly 13 life loses the game, then each player gains 1 life.
        //  • Each player with exactly 13 life loses the game, then each player loses 1 life."
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Each player with exactly 13 life loses the game, then each player gains 1 life.",
                        SequenceEffect.of(playersAtThirteenLose(), new GainLifeEffect(1, Subjects.eachPlayer()))),
                new ChooseOneEffect.ChooseOneOption(
                        "Each player with exactly 13 life loses the game, then each player loses 1 life.",
                        SequenceEffect.of(playersAtThirteenLose(), new LoseLifeEffect(1, Subjects.eachPlayer())))
        )));
    }

    private static CardEffect playersAtThirteenLose() {
        return new ControllerLosesGameEffect(
                Subjects.matching(Subjects.eachPlayer(), new PlayerLifeExactlyPredicate(13)));
    }
}
