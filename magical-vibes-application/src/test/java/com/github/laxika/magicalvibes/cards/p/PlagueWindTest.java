package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DarksteelSentinel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlagueWind.class, GrizzlyBears.class, HowlingMine.class, DarksteelSentinel.class})
class PlagueWindTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Plague Wind puts it on the stack as a sorcery")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new PlagueWind(), "{7}{B}{B}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Plague Wind");
    }

    @Test
    @DisplayName("Plague Wind destroys only creatures you do not control")
    void destroysOnlyOpponentsCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new PlagueWind(), "{7}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Plague Wind does not destroy noncreature permanents you do not control")
    void doesNotDestroyOpponentsNonCreatures() {
        harness.addToBattlefield(player2, new HowlingMine());
        harness.castFromHand(player1, new PlagueWind(), "{7}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Howling Mine");
    }

    @Test
    @DisplayName("Creatures destroyed by Plague Wind cannot be regenerated")
    void ignoresRegenerationShields() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent opposingBears = findPermanent(player2, "Grizzly Bears");
        opposingBears.setRegenerationShield(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new PlagueWind(), "{7}{B}{B}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Indestructible creatures you do not control survive Plague Wind")
    void indestructibleOpponentCreaturesSurvive() {
        harness.addToBattlefield(player2, new DarksteelSentinel());

        harness.castFromHand(player1, new PlagueWind(), "{7}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Darksteel Sentinel");
    }
}
