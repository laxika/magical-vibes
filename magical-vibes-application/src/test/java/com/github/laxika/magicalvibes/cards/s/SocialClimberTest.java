package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({SocialClimber.class, GrizzlyBears.class})
class SocialClimberTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when another creature you control enters")
    void gainsLifeOnAllyCreatureEnter() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new SocialClimber());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Does not gain life when it enters")
    void noLifeOnOwnEnter() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SocialClimber()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not gain life when an opponent's creature enters")
    void noLifeOnOpponentCreatureEnter() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new SocialClimber());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }
}
