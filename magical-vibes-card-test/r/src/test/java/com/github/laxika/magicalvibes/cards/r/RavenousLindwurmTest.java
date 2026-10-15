package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({RavenousLindwurm.class})
class RavenousLindwurmTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldCausesItsControllerToGainFourLife() {
        harness.setLife(player1, 12);
        harness.setLife(player2, 17);
        harness.setHand(player1, List.of(new RavenousLindwurm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 17);
    }

    @Test
    void lifeIsGainedOnlyWhenTheEnterTriggerResolves() {
        harness.setLife(player1, 12);
        harness.setHand(player1, List.of(new RavenousLindwurm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.assertLife(player1, 12);
        harness.assertNotOnBattlefield(player1, "Ravenous Lindwurm");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ravenous Lindwurm");
        harness.assertLife(player1, 12);

        resolveAllTriggers();
        harness.assertLife(player1, 16);
    }

    @Test
    void theOtherPlayersLindwurmGainsLifeForThatPlayer() {
        harness.forceActivePlayer(player2);
        harness.setLife(player1, 12);
        harness.setLife(player2, 17);
        harness.setHand(player2, List.of(new RavenousLindwurm()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 21);
    }
}
