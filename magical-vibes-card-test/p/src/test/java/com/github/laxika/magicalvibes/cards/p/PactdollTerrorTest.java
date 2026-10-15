package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GlazeFiend;
import com.github.laxika.magicalvibes.cards.l.LoxodonSurveyor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PactdollTerror.class, GlazeFiend.class, LoxodonSurveyor.class})
class PactdollTerrorTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry makes each opponent lose 1 life and its controller gain 1 life")
    void ownEntryDrainsEachOpponent() {
        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.castFromHand(player1, new PactdollTerror(), "{3}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore + 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore - 1);
    }

    @Test
    @DisplayName("Another artifact entering under its controller's control triggers the drain")
    void allyArtifactEntryDrainsEachOpponent() {
        harness.addToBattlefield(player1, new PactdollTerror());
        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.castFromHand(player1, new GlazeFiend(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore + 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore - 1);
    }

    @Test
    @DisplayName("An artifact entering under an opponent's control does not trigger it")
    void opponentArtifactEntryDoesNotDrain() {
        harness.addToBattlefield(player1, new PactdollTerror());
        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new GlazeFiend(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore);
    }

    @Test
    @DisplayName("Another artifact's entry drains and gains life in one ability resolution")
    void allyArtifactDrainResolvesAsOneAbility() {
        harness.addToBattlefield(player1, new PactdollTerror());
        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.castFromHand(player1, new GlazeFiend(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore);

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore + 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore - 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A nonartifact creature entering under its controller's control does not drain")
    void nonartifactCreatureEntryDoesNotDrain() {
        harness.addToBattlefield(player1, new PactdollTerror());
        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.castFromHand(player1, new LoxodonSurveyor(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore);
        assertThat(gd.stack).isEmpty();
    }

}
