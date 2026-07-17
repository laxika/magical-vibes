package com.github.laxika.magicalvibes;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.r.Rhox;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("scryfall")
class CombatDamageAssignmentTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Two blockers: player distributes damage freely")
    void twoBlockersPlayerDistributesDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new LlanowarElves());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        List<Permanent> defBf = gd.playerBattlefields.get(player2.getId());
        Permanent blocker1 = defBf.get(0);
        Permanent blocker2 = defBf.get(1);
        blocker1.setBlocking(true);
        blocker1.addBlockingTarget(0);
        blocker2.setBlocking(true);
        blocker2.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Advance from DECLARE_BLOCKERS → COMBAT_DAMAGE (paused for assignment)
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.CombatDamageAssignment.class);

        // Assign 1 damage to each blocker (kills both 1/1s)
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker1.getId(), 1,
                blocker2.getId(), 1
        ));

        // Both blockers should be dead
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        // Player took no damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Two blockers: player can assign all damage to one blocker")
    void twoBlockersAllDamageToOne() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new LlanowarElves());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        List<Permanent> defBf = gd.playerBattlefields.get(player2.getId());
        Permanent blocker1 = defBf.get(0);
        Permanent blocker2 = defBf.get(1);
        blocker1.setBlocking(true);
        blocker1.addBlockingTarget(0);
        blocker2.setBlocking(true);
        blocker2.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Assign all 2 damage to the first blocker
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker1.getId(), 2
        ));

        // First blocker should be dead
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getId().equals(blocker1.getId())).isEmpty();
        // Second blocker should survive
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getId().equals(blocker2.getId())).hasSize(1);
        // Player took no damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Trample creature: must assign lethal to blocker, excess to player")
    void trampleCreatureAssignsExcessToPlayer() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.CombatDamageAssignment.class);

        // Assign lethal (2) to blocker, remaining 6 to defending player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 6
        ));

        // Blocker should be dead
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Player took 6 trample damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Trample creature: player can over-assign to blocker")
    void trampleCreatureCanOverAssignToBlocker() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Over-assign to blocker: 5 to blocker (only 2 needed), 3 to player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 5,
                player2.getId(), 3
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Trample, two blockers: all damage may go to one blocker when none goes to the player")
    void trampleTwoBlockersAllDamageToOneBlockerNoOverflow() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        List<Permanent> defBf = gd.playerBattlefields.get(player2.getId());
        Permanent blocker1 = defBf.get(0);
        Permanent blocker2 = defBf.get(1);
        blocker1.setBlocking(true);
        blocker1.addBlockingTarget(0);
        blocker2.setBlocking(true);
        blocker2.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.CombatDamageAssignment.class);

        // CR 510.1c: lethal to each blocker is only required to assign damage to the player.
        // With no damage assigned to the player, all 8 may be piled onto one blocker.
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker1.getId(), 8));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getId().equals(blocker1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getId().equals(blocker2.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Trample, two blockers: assigning to the player still requires lethal to every blocker")
    void trampleOverflowStillRequiresLethalToEachBlocker() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        List<Permanent> defBf = gd.playerBattlefields.get(player2.getId());
        Permanent blocker1 = defBf.get(0);
        Permanent blocker2 = defBf.get(1);
        blocker1.setBlocking(true);
        blocker1.addBlockingTarget(0);
        blocker2.setBlocking(true);
        blocker2.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Blocker2 gets 1 (lethal is 2) while 5 is assigned to the player: illegal.
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                harness.handleCombatDamageAssigned(player1, 0, Map.of(
                        blocker1.getId(), 2,
                        blocker2.getId(), 1,
                        player2.getId(), 5
                )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trample");

        // A legal assignment afterwards still works.
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker1.getId(), 2,
                blocker2.getId(), 2,
                player2.getId(), 4
        ));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Single blocker without trample: auto-resolved without assignment prompt")
    void singleBlockerNoTrampleAutoResolved() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Should auto-resolve without prompting
        harness.passBothPriorities();

        // Blocker should be dead (2 damage kills a 1/1)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        // Player took no damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Unblocked creature: auto-resolved without assignment prompt")
    void unblockedCreatureAutoResolved() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Player took 2 damage from unblocked creature
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("First-strike trampler: damage assignment is prompted in the first-strike step")
    void firstStrikeTramplerPromptsInFirstStrikeStep() {
        harness.setLife(player2, 20);
        Card fsTrampler = new Card();
        fsTrampler.setName("Test FS Trampler");
        fsTrampler.setType(CardType.CREATURE);
        fsTrampler.setManaCost("{3}{R}");
        fsTrampler.setPower(5);
        fsTrampler.setToughness(5);
        fsTrampler.setKeywords(EnumSet.of(Keyword.FIRST_STRIKE, Keyword.TRAMPLE));
        harness.addToBattlefield(player1, fsTrampler);
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // The first-strike damage step pauses for the assignment instead of auto-assigning.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class)).isNotNull();
        assertThat(gd.combatDamageFirstStrikeAssignmentPhase).isTrue();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 3
        ));

        // First-strike damage resolved: blocker dead, 3 trampled over; the regular step deals
        // nothing more (the attacker has first strike only).
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Creature blocking two attackers: the defending player divides its combat damage")
    void multiBlockingCreatureDefenderDividesDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card bigBlocker = new Card();
        bigBlocker.setName("Test Big Blocker");
        bigBlocker.setType(CardType.CREATURE);
        bigBlocker.setManaCost("{2}{G}");
        bigBlocker.setPower(3);
        bigBlocker.setToughness(5);
        harness.addToBattlefield(player2, bigBlocker);

        List<Permanent> atkBf = gd.playerBattlefields.get(player1.getId());
        Permanent attacker1 = atkBf.get(0);
        Permanent attacker2 = atkBf.get(1);
        attacker1.setSummoningSick(false);
        attacker1.setAttacking(true);
        attacker2.setSummoningSick(false);
        attacker2.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        blocker.addBlockingTarget(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // CR 510.1d — the defending player is prompted to divide the blocker's damage.
        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        assertThat(prompt.totalDamage()).isEqualTo(3);

        // Put all 3 damage on the second attacker: it dies, the first survives untouched.
        harness.handleCombatDamageAssigned(player2, 0, Map.of(attacker2.getId(), 3));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .contains(attacker1.getId())
                .doesNotContain(attacker2.getId());
        // The 3/5 blocker takes 2+2 and survives; the defending player takes no damage.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Test Big Blocker"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Rhox blocked: can assign all damage to defending player via assign-as-unblocked")
    void rhoxBlockedAssignAllToPlayer() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Rhox());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent rhox = gd.playerBattlefields.get(player1.getId()).getFirst();
        rhox.setSummoningSick(false);
        rhox.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.CombatDamageAssignment.class);

        // Assign all damage to defending player (using assign-as-though-unblocked)
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 5));

        // Blocker survived (no damage assigned to it)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Player took all 5 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Rhox blocked: can assign all damage to blocker instead")
    void rhoxBlockedAssignAllToBlocker() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Rhox());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent rhox = gd.playerBattlefields.get(player1.getId()).getFirst();
        rhox.setSummoningSick(false);
        rhox.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Assign all damage to blocker (declines to use assign-as-unblocked ability)
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 5));

        // Blocker should be dead (5 damage > 2 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Player took no damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
