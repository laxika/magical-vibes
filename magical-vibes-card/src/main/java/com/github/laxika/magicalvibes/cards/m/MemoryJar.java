package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileEachPlayerHandFaceDownAndReturnAtNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "ULG", collectorNumber = "129")
public class MemoryJar extends Card {

    public MemoryJar() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new ExileEachPlayerHandFaceDownAndReturnAtNextEndStepEffect(),
                        new EachPlayerDrawsCardEffect(7)
                ),
                "{T}, Sacrifice Memory Jar: Each player exiles all cards from their hand face down and draws seven cards. At the beginning of the next end step, each player discards their hand and returns to their hand each card they exiled this way."
        ));
    }
}
