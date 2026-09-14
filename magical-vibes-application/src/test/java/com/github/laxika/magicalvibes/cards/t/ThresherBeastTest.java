package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.PygmyRazorback;
import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.cards.w.WintermoonMesa;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThresherBeast.class, PygmyRazorback.class, RhysticCave.class, WintermoonMesa.class})
class ThresherBeastTest extends BaseCardTest {

    @Test
    @DisplayName("The defending player chooses a land to sacrifice when Thresher Beast becomes blocked")
    void defendingPlayerChoosesLandToSacrifice() {
        Permanent attacker = addCreatureReady(player1, new ThresherBeast());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent blocker = addCreatureReady(player2, new PygmyRazorback());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        Permanent wintermoonMesa = harness.addToBattlefieldAndReturn(player2, new WintermoonMesa());
        Permanent rhysticCave = harness.addToBattlefieldAndReturn(player2, new RhysticCave());
        harness.addToBattlefield(player2, new PygmyRazorback());

        declareBlocks(attacker, List.of(blocker));

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds())
                .containsExactlyInAnyOrder(wintermoonMesa.getId(), rhysticCave.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(wintermoonMesa.getId()));

        harness.assertNotOnBattlefield(player2, "Wintermoon Mesa");
        harness.assertOnBattlefield(player2, "Rhystic Cave");
        harness.assertOnBattlefield(player2, "Pygmy Razorback");
        harness.assertOnBattlefield(player1, "Rhystic Cave");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Multiple blockers still cause only one land sacrifice")
    void multipleBlockersCauseOneSacrifice() {
        Permanent attacker = addCreatureReady(player1, new ThresherBeast());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent firstBlocker = addCreatureReady(player2, new PygmyRazorback());
        Permanent secondBlocker = addCreatureReady(player2, new PygmyRazorback());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player2, new WintermoonMesa());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player2, new RhysticCave());

        declareBlocks(attacker, List.of(firstBlocker, secondBlocker));

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstLand.getId(), secondLand.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(firstLand.getId()));

        harness.assertNotOnBattlefield(player2, "Wintermoon Mesa");
        harness.assertOnBattlefield(player2, "Rhystic Cave");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A blocked Thresher Beast does not prompt when the defending player controls no lands")
    void blockedWithNoLandsDoesNotPrompt() {
        Permanent attacker = addCreatureReady(player1, new ThresherBeast());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent blocker = addCreatureReady(player2, new PygmyRazorback());

        declareBlocks(attacker, List.of(blocker));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Thresher Beast does not trigger when it is unblocked")
    void unblockedDoesNotTrigger() {
        Permanent attacker = addCreatureReady(player1, new ThresherBeast());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.addToBattlefield(player2, new RhysticCave());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Rhystic Cave");
    }

    private void declareBlocks(Permanent attacker, List<Permanent> blockers) {
        prepareDeclareBlockers();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        List<BlockerAssignment> assignments = blockers.stream()
                .map(blocker -> new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker), attackerIndex))
                .toList();
        gs.declareBlockers(gd, player2, assignments);
        harness.passBothPriorities();
    }
}
