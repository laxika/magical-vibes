package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.p.PiratesCutlass;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DesperateCastaways.class, PiratesCutlass.class})
class DesperateCastawaysTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack when controller controls an artifact")
    void canAttackWhenControllerControlsArtifact() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DesperateCastaways());
        harness.addToBattlefield(player1, new PiratesCutlass());

        declareAttackers(player1, List.of(0));

        // Attack went through — defender takes 2 damage (power of Desperate Castaways)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Cannot attack when controller does not control an artifact")
    void cannotAttackWithoutArtifact() {
        addCreatureReady(player1, new DesperateCastaways());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot attack when only opponent controls an artifact")
    void cannotAttackWhenOnlyOpponentControlsArtifact() {
        addCreatureReady(player1, new DesperateCastaways());
        harness.addToBattlefield(player2, new PiratesCutlass());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block without controlling an artifact")
    void canBlockWithoutArtifact() {
        harness.setLife(player1, 20);
        addCreatureReady(player2, new DesperateCastaways());
        // Give opponent an artifact so the opponent's Castaways can attack
        harness.addToBattlefield(player2, new PiratesCutlass());

        // Player1 has their own Castaways with no artifact — still should be able to block
        addCreatureReady(player1, new DesperateCastaways());

        // Declare the opponent's creature as attacker
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        // Now declare blocker
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        // Blocker was accepted — the creature is blocking
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A tapped artifact still allows attacking")
    void canAttackWithTappedArtifact() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DesperateCastaways());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PiratesCutlass());
        artifact.setTapped(true);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Artifacts in hand and graveyard do not allow attacking")
    void cannotAttackWithArtifactsOutsideBattlefield() {
        addCreatureReady(player1, new DesperateCastaways());
        harness.setHand(player1, List.of(new PiratesCutlass()));
        harness.setGraveyard(player1, List.of(new PiratesCutlass()));

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
