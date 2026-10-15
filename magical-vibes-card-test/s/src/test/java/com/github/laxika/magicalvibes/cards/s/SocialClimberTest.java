package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BrokersInitiate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({SocialClimber.class, BrokersInitiate.class})
class SocialClimberTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when another creature you control enters")
    void gainsLifeOnAllyCreatureEnter() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new SocialClimber());
        harness.setHand(player1, List.of(new BrokersInitiate()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

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
        harness.enterBattlefieldAndReturn(player2, new BrokersInitiate());

        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each Social Climber triggers for another creature")
    void multipleClimbersEachGainLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new SocialClimber());
        harness.addToBattlefield(player1, new SocialClimber());

        harness.enterBattlefieldAndReturn(player1, new BrokersInitiate());
        harness.assertLife(player1, 20);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An entering Social Climber triggers the existing copy only")
    void enteringClimberTriggersOtherCopyOnly() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new SocialClimber());
        harness.setHand(player1, List.of(new SocialClimber()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Triggers for each creature entering in the same turn")
    void gainsLifeForEveryEntry() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new SocialClimber());

        harness.enterBattlefieldAndReturn(player1, new BrokersInitiate());
        resolveAllTriggers();
        harness.assertLife(player1, 21);

        harness.enterBattlefieldAndReturn(player1, new BrokersInitiate());
        resolveAllTriggers();
        harness.assertLife(player1, 22);
    }
}
