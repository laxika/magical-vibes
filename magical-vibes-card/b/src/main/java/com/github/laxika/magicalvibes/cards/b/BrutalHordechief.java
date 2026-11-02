package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseBlockersThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentCreatureBlocksThisTurnIfAbleEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "64")
@CardRegistration(set = "C16", collectorNumber = "108")
public class BrutalHordechief extends Card {

    public BrutalHordechief() {
        // Whenever a creature you control attacks, defending player loses 1 life and you gain 1 life.
        addTrigger(Triggers.creatureYouControlAttacks(),
                new LoseLifeEffect(1, LoseLifeRecipient.DEFENDING_PLAYER));
        addTrigger(Triggers.creatureYouControlAttacks(), new GainLifeEffect(1));

        // {3}{R/W}{R/W}: Creatures your opponents control block this turn if able, and you choose
        // how those creatures block.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R/W}{R/W}",
                List.of(
                        new EachOpponentCreatureBlocksThisTurnIfAbleEffect(),
                        new ChooseBlockersThisCombatEffect(true)
                ),
                "{3}{R/W}{R/W}: Creatures your opponents control block this turn if able, and you choose how those creatures block."
        ));
    }
}
