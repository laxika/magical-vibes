package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlagueWind.class, GrizzlyBears.class, HowlingMine.class})
class PlagueWindTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Plague Wind puts it on the stack as a sorcery")
    void castingPutsItOnStack() {
        PlagueWind plagueWind = new PlagueWind();
        harness.castFromHand(player1, plagueWind, "{7}{B}{B}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isSameAs(plagueWind);
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
    @DisplayName("Plague Wind resolves when no creatures are on the battlefield")
    void resolvesWithoutCreatures() {
        harness.castFromHand(player1, new PlagueWind(), "{7}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Plague Wind");
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
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opposingBears.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        harness.castFromHand(player1, new PlagueWind(), "{7}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
