package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
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

@CardUsed({Shatterstorm.class, HowlingMine.class, Ornithopter.class, GrizzlyBears.class})
class ShatterstormTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Shatterstorm puts it on the stack as a sorcery")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new Shatterstorm(), "{2}{R}{R}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
    }

    @Test
    @DisplayName("Shatterstorm destroys artifacts controlled by both players")
    void destroysArtifactsFromBothPlayers() {
        harness.addToBattlefield(player1, new HowlingMine());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.castFromHand(player1, new Shatterstorm(), "{2}{R}{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Howling Mine");
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player1, "Howling Mine");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Shatterstorm does not destroy nonartifact permanents")
    void doesNotDestroyNonArtifacts() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.castFromHand(player1, new Shatterstorm(), "{2}{R}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Artifacts destroyed by Shatterstorm cannot be regenerated")
    void ignoresRegenerationShields() {
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent ornithopter = findPermanent(player1, "Ornithopter");
        ornithopter.setRegenerationShield(2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new Shatterstorm(), "{2}{R}{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Indestructible artifacts survive Shatterstorm")
    void indestructibleArtifactsSurvive() {
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        ornithopter.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.castFromHand(player1, new Shatterstorm(), "{2}{R}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ornithopter");
    }
}

