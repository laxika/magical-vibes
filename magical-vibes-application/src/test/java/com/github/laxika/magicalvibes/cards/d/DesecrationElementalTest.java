package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AuriokChampion;
import com.github.laxika.magicalvibes.cards.m.MyrServitor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DesecrationElemental.class, AuriokChampion.class, DrossCrocodile.class, MyrServitor.class})
class DesecrationElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Controller sacrifices a creature when they cast a spell")
    void controllerSacrificesCreatureOnOwnSpellCast() {
        harness.addToBattlefield(player1, new DesecrationElemental());
        Permanent servitor = harness.addToBattlefieldAndReturn(player1, new MyrServitor());

        harness.castFromHand(player1, new MyrServitor(), "{1}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, servitor.getId());

        harness.assertInGraveyard(player1, "Myr Servitor");
        harness.assertOnBattlefield(player1, "Desecration Elemental");
    }

    @Test
    @DisplayName("An opponent casting a spell still makes the Elemental's controller sacrifice")
    void opponentSpellMakesControllerSacrifice() {
        harness.addToBattlefield(player1, new DesecrationElemental());
        Permanent servitor = harness.addToBattlefieldAndReturn(player1, new MyrServitor());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new MyrServitor(), "{1}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handlePermanentChosen(player1, servitor.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Myr Servitor");
        harness.assertOnBattlefield(player2, "Myr Servitor");
    }

    @Test
    @DisplayName("The trigger sacrifices the Elemental if it is the only creature")
    void sourceCanBeSacrificed() {
        harness.addToBattlefield(player1, new DesecrationElemental());

        harness.castFromHand(player1, new MyrServitor(), "{1}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Desecration Elemental");
    }

    @Test
    @DisplayName("Fear prevents a nonblack nonartifact creature from blocking")
    void fearRejectsNonblackNonartifactBlocker() {
        Permanent elemental = addCreatureReady(player1, new DesecrationElemental());
        Permanent blocker = addCreatureReady(player2, new AuriokChampion());

        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(elemental);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(fear)");
    }

    @Test
    @DisplayName("Fear allows artifact and black creatures to block")
    void fearAllowsArtifactAndBlackBlockers() {
        Permanent elemental = addCreatureReady(player1, new DesecrationElemental());
        Permanent artifactBlocker = addCreatureReady(player2, new MyrServitor());
        Permanent blackBlocker = addCreatureReady(player2, new DrossCrocodile());

        declareAttackersAndPrepareBlockers(List.of(0));

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(elemental);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(artifactBlocker), attackerIndex),
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blackBlocker), attackerIndex)));

        assertThat(artifactBlocker.isBlocking()).isTrue();
        assertThat(blackBlocker.isBlocking()).isTrue();
    }
}
