package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.ai.simulation.GameSimulator;
import com.github.laxika.magicalvibes.ai.simulation.SimulationAction;
import com.github.laxika.magicalvibes.cards.a.ArmoredAscension;
import com.github.laxika.magicalvibes.cards.b.BerserkersOfBloodRidge;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.PhantomWarrior;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("scryfall")
class GameSimulatorTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;
    private GameSimulator simulator;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        simulator = GameSimulator.forQueryService(harness.getGameQueryService());
    }

    @Test
    @DisplayName("Legal actions in main phase include castable spells and pass")
    void legalActionsMainPhaseIncludesCastableSpells() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.stack.clear();

        List<SimulationAction> actions = simulator.getLegalActions(gd, player1.getId());

        // Should have at least one PlayCard and one PassPriority
        assertThat(actions).anyMatch(a -> a instanceof SimulationAction.PlayCard);
        assertThat(actions).anyMatch(a -> a instanceof SimulationAction.PassPriority);
    }

    @Test
    @DisplayName("Legal actions exclude requiresCreatureMana card when only land mana available")
    void legalActionsExcludeCreatureManaCardWithLandMana() {
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.m.MyrSuperion()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.stack.clear();

        List<SimulationAction> actions = simulator.getLegalActions(gd, player1.getId());

        // Only pass — Myr Superion not castable without creature mana
        assertThat(actions).noneMatch(a -> a instanceof SimulationAction.PlayCard);
        assertThat(actions).anyMatch(a -> a instanceof SimulationAction.PassPriority);
    }

    @Test
    @DisplayName("Legal actions include requiresCreatureMana card when creature mana dorks available")
    void legalActionsIncludeCreatureManaCardWithCreatureMana() {
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.m.MyrSuperion()));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.stack.clear();

        // Add two Llanowar Elves as creature mana sources
        com.github.laxika.magicalvibes.model.Permanent elf1 =
                new com.github.laxika.magicalvibes.model.Permanent(
                        new com.github.laxika.magicalvibes.cards.l.LlanowarElves());
        elf1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(elf1);

        com.github.laxika.magicalvibes.model.Permanent elf2 =
                new com.github.laxika.magicalvibes.model.Permanent(
                        new com.github.laxika.magicalvibes.cards.l.LlanowarElves());
        elf2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(elf2);

        List<SimulationAction> actions = simulator.getLegalActions(gd, player1.getId());

        // Should include PlayCard for Myr Superion
        assertThat(actions).anyMatch(a -> a instanceof SimulationAction.PlayCard);
    }

    @Test
    @DisplayName("Legal actions in main phase with empty hand only has pass")
    void legalActionsMainPhaseEmptyHandOnlyPass() {
        harness.setHand(player1, List.of());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.stack.clear();

        List<SimulationAction> actions = simulator.getLegalActions(gd, player1.getId());

        assertThat(actions).hasSize(1);
        assertThat(actions.getFirst()).isInstanceOf(SimulationAction.PassPriority.class);
    }

    @Test
    @DisplayName("Apply action does not affect original GameData")
    void applyActionDoesNotAffectOriginal() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.stack.clear();

        int origHandSize = gd.playerHands.get(player1.getId()).size();
        int origBattlefieldSize = gd.playerBattlefields.get(player1.getId()).size();

        // Apply action to a copy
        GameData copy = gd.simulationCopy();
        simulator.applyAction(copy, player1.getId(), new SimulationAction.PlayCard(0, null, 0));

        // Original unchanged
        assertThat(gd.playerHands.get(player1.getId())).hasSize(origHandSize);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(origBattlefieldSize);
    }

    @Test
    @DisplayName("Terminal detection: game over when player at 0 life")
    void terminalDetectionZeroLife() {
        gd.playerLifeTotals.put(player2.getId(), 0);
        assertThat(simulator.isTerminal(gd)).isTrue();
    }

    @Test
    @DisplayName("Terminal detection: game not over at positive life")
    void terminalDetectionPositiveLife() {
        assertThat(simulator.isTerminal(gd)).isFalse();
    }

    @Test
    @DisplayName("Evaluate returns higher score when AI is winning")
    void evaluateHigherWhenAiWinning() {
        gd.playerLifeTotals.put(player1.getId(), 20);
        gd.playerLifeTotals.put(player2.getId(), 5);

        double score = simulator.evaluate(gd, player1.getId());
        assertThat(score).isGreaterThan(0.5);
    }

    @Test
    @DisplayName("Evaluate returns lower score when AI is losing")
    void evaluateLowerWhenAiLosing() {
        gd.playerLifeTotals.put(player1.getId(), 3);
        gd.playerLifeTotals.put(player2.getId(), 20);

        double score = simulator.evaluate(gd, player1.getId());
        assertThat(score).isLessThan(0.5);
    }

    // ===== Aura targeting =====

    private void setUpMainPhase(Player activePlayer) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(activePlayer);
        gd.stack.clear();
    }

    @Test
    @DisplayName("Beneficial aura targets own creature, not opponent's")
    void beneficialAuraTargetsOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ArmoredAscension()));
        harness.addMana(player1, ManaColor.WHITE, 5); // 4W
        setUpMainPhase(player1);

        List<SimulationAction> actions = simulator.getLegalActions(gd, player1.getId());

        assertThat(actions).anyMatch(a -> a instanceof SimulationAction.PlayCard pc
                && pc.targetId().equals(ownCreature.getId()));
    }

    @Test
    @DisplayName("Beneficial aura picks own creature with highest toughness")
    void beneficialAuraPicksHighestToughnessOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2
        Permanent serraAngel = harness.addToBattlefieldAndReturn(player1, new SerraAngel()); // 4/4
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ArmoredAscension()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        setUpMainPhase(player1);

        List<SimulationAction> actions = simulator.getLegalActions(gd, player1.getId());

        assertThat(actions).anyMatch(a -> a instanceof SimulationAction.PlayCard pc
                && pc.targetId().equals(serraAngel.getId()));
    }

    @Test
    @DisplayName("Beneficial aura generates no PlayCard action when AI has no creatures")
    void beneficialAuraNoActionWhenNoOwnCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ArmoredAscension()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        setUpMainPhase(player1);

        List<SimulationAction> actions = simulator.getLegalActions(gd, player1.getId());

        assertThat(actions).noneMatch(a -> a instanceof SimulationAction.PlayCard);
    }

    @Test
    @DisplayName("Detrimental aura targets opponent's creature, not own")
    void detrimentalAuraTargetsOpponentCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2); // 1W
        setUpMainPhase(player1);

        List<SimulationAction> actions = simulator.getLegalActions(gd, player1.getId());

        assertThat(actions).anyMatch(a -> a instanceof SimulationAction.PlayCard pc
                && pc.targetId().equals(opponentCreature.getId()));
    }

    // ===== Blocker declaration actions =====

    private void setUpBlockerDeclaration(Player defender, Player attacker) {
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.forceActivePlayer(attacker);
        gd.interaction.beginBlockerDeclaration(defender.getId());
    }

    @Nested
    @DisplayName("Blocker declaration legal actions")
    class BlockerDeclarationActions {

        @Test
        @DisplayName("Blocker actions always include no-block option")
        void blockerActionsAlwaysIncludeNoBlock() {
            // One blocker for player1, one attacker for player2
            harness.addToBattlefield(player1, new GrizzlyBears());
            gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(false));

            Permanent attacker = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);

            setUpBlockerDeclaration(player1, player2);

            List<SimulationAction> actions = simulator.getLegalActions(gd, player1.getId());

            assertThat(actions).anyMatch(a -> a instanceof SimulationAction.DeclareBlockers db
                    && db.blockerAssignments().isEmpty());
        }

        @Test
        @DisplayName("Blocker actions include best blocking from CombatSimulator")
        void blockerActionsIncludeBestBlocking() {
            // Two blockers for player1, one attacker for player2
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player1, new SerraAngel());
            gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(false));

            Permanent attacker = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);

            setUpBlockerDeclaration(player1, player2);

            List<SimulationAction> actions = simulator.getLegalActions(gd, player1.getId());

            // Should have at least one non-empty DeclareBlockers (the best from CombatSimulator)
            assertThat(actions).anyMatch(a -> a instanceof SimulationAction.DeclareBlockers db
                    && !db.blockerAssignments().isEmpty());
        }

        @Test
        @DisplayName("Multiple blockers generate individual single-block options for biggest attacker")
        void multipleBlockersGenerateSingleBlockOptions() {
            // Two blockers for player1
            harness.addToBattlefield(player1, new GrizzlyBears());  // 2/2 — index 0
            harness.addToBattlefield(player1, new SerraAngel());    // 4/4 — index 1
            gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(false));

            // Two attackers for player2: small (2/1) and big (4/4)
            Permanent smallAttacker = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());
            smallAttacker.setSummoningSick(false);
            smallAttacker.setAttacking(true);

            Permanent bigAttacker = harness.addToBattlefieldAndReturn(player2, new BerserkersOfBloodRidge());
            bigAttacker.setSummoningSick(false);
            bigAttacker.setAttacking(true);

            setUpBlockerDeclaration(player1, player2);

            List<SimulationAction> actions = simulator.getLegalActions(gd, player1.getId());

            // Should have individual "block only the biggest attacker" options
            // BerserkersOfBloodRidge (4/4) is the biggest — at battlefield index 1
            // Each blocker should get an option to solo-block it
            long singleBlockActions = actions.stream()
                    .filter(a -> a instanceof SimulationAction.DeclareBlockers db
                            && db.blockerAssignments().size() == 1)
                    .count();

            // At least one single-block-on-biggest-attacker option should exist
            assertThat(singleBlockActions).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("No single-block options when no attackers")
        void noSingleBlockOptionsWhenNoAttackers() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player1, new SerraAngel());
            gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(false));

            // No attackers on opponent's side
            setUpBlockerDeclaration(player1, player2);

            List<SimulationAction> actions = simulator.getLegalActions(gd, player1.getId());

            // Only the no-block option should exist (no best-block either since no attackers)
            assertThat(actions).hasSize(1);
            assertThat(actions.getFirst()).isInstanceOf(SimulationAction.DeclareBlockers.class);
            assertThat(((SimulationAction.DeclareBlockers) actions.getFirst()).blockerAssignments()).isEmpty();
        }

        @Test
        @DisplayName("Tapped creatures are excluded from blocker options")
        void tappedCreaturesExcludedFromBlockerOptions() {
            // One untapped blocker and one tapped blocker
            Permanent untapped = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            untapped.setSummoningSick(false);

            Permanent tapped = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
            tapped.setSummoningSick(false);
            tapped.tap();

            Permanent attacker = harness.addToBattlefieldAndReturn(player2, new BerserkersOfBloodRidge());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);

            setUpBlockerDeclaration(player1, player2);

            List<SimulationAction> actions = simulator.getLegalActions(gd, player1.getId());

            // Single-block options should only reference the untapped blocker (index 0)
            // not the tapped one (index 1)
            List<SimulationAction.DeclareBlockers> singleBlocks = actions.stream()
                    .filter(a -> a instanceof SimulationAction.DeclareBlockers db
                            && db.blockerAssignments().size() == 1)
                    .map(a -> (SimulationAction.DeclareBlockers) a)
                    .toList();

            for (SimulationAction.DeclareBlockers db : singleBlocks) {
                int blockerIdx = db.blockerAssignments().getFirst()[0];
                // Tapped SerraAngel is at index 1 — should not appear
                assertThat(blockerIdx).isNotEqualTo(1);
            }
        }

        @Test
        @DisplayName("Blocker actions have at least 3 options with multiple blockers and attackers")
        void blockerActionsHaveMultipleOptionsWithMultipleCreatures() {
            // Two blockers
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player1, new SerraAngel());
            gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(false));

            // Two attackers
            Permanent att1 = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());
            att1.setSummoningSick(false);
            att1.setAttacking(true);

            Permanent att2 = harness.addToBattlefieldAndReturn(player2, new BerserkersOfBloodRidge());
            att2.setSummoningSick(false);
            att2.setAttacking(true);

            setUpBlockerDeclaration(player1, player2);

            List<SimulationAction> actions = simulator.getLegalActions(gd, player1.getId());

            // Should have at least: no-block, best-block, and single-block options
            assertThat(actions.size()).isGreaterThanOrEqualTo(3);
        }
    }
}
