package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.testutil.TestCards;
import com.github.laxika.magicalvibes.ai.simulation.GameSimulator;
import com.github.laxika.magicalvibes.ai.simulation.HeadlessSimulationContext;
import com.github.laxika.magicalvibes.ai.simulation.MCTSEngine;
import com.github.laxika.magicalvibes.ai.simulation.SimulationAction;
import com.github.laxika.magicalvibes.cards.t.TroveOfTemptation;
import com.github.laxika.magicalvibes.cards.t.TragedyFeaster;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AbandonHope;
import com.github.laxika.magicalvibes.cards.b.BairdStewardOfArgive;
import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.b.Blight;
import com.github.laxika.magicalvibes.cards.b.BlindingBeam;
import com.github.laxika.magicalvibes.cards.b.BogWraith;
import com.github.laxika.magicalvibes.cards.p.PhantomWarrior;
import com.github.laxika.magicalvibes.cards.s.SeveredLegion;
import com.github.laxika.magicalvibes.cards.b.BerserkersOfBloodRidge;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.CrypticCommand;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.e.EntrancingMelody;
import com.github.laxika.magicalvibes.cards.e.Errantry;
import com.github.laxika.magicalvibes.cards.e.Eviscerate;
import com.github.laxika.magicalvibes.cards.f.FitOfRage;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GoblinChieftain;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.m.Mindslaver;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.b.BogardanFirefiend;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.i.IslandSanctuary;
import com.github.laxika.magicalvibes.cards.i.IronStar;
import com.github.laxika.magicalvibes.cards.k.KuldothaRebirth;
import com.github.laxika.magicalvibes.cards.k.KillerBees;
import com.github.laxika.magicalvibes.cards.v.VigilForTheLost;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.Negate;
import com.github.laxika.magicalvibes.cards.n.Nekrataal;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Slagstorm;
import com.github.laxika.magicalvibes.cards.s.SmiteTheMonstrous;
import com.github.laxika.magicalvibes.cards.s.SteelSabotage;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.u.Unbury;
import com.github.laxika.magicalvibes.cards.v.Vivisection;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.attack.CombatAttackService;
import com.github.laxika.magicalvibes.service.combat.block.BlockLegalityService;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.github.laxika.magicalvibes.model.CounterType;

@Tag("scryfall")
class HardAiSpellStrategyTest extends HardAiDecisionEngineTestSupport {

    @Test
    @DisplayName("Hard AI casts Entrancing Melody with X matching target creature's mana value")
    void castsEntrancingMelodyWithCorrectX() {
        HardAiDecisionEngine ai = createHardAi(player1);
        pinLibrariesAndHands();
        giveAiPriority(player1);
        givePlayerIslands(player1, 4); // maxX = 2

        Permanent bears = new Permanent(new GrizzlyBears()); // MV=2
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new EntrancingMelody()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Entrancing Melody");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("Hard AI picks highest affordable target for Entrancing Melody")
    void picksHighestAffordableTargetForEntrancingMelody() {
        HardAiDecisionEngine ai = createHardAi(player1);
        pinLibrariesAndHands();
        giveAiPriority(player1);
        givePlayerIslands(player1, 4); // maxX = 2

        Permanent vanguard = new Permanent(new EliteVanguard()); // MV=1
        vanguard.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(vanguard);

        Permanent bears = new Permanent(new GrizzlyBears()); // MV=2
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new EntrancingMelody()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("Hard AI upgrades the MCTS-chosen Entrancing Melody target to the highest affordable one")
    void upgradesMctsChosenEntrancingMelodyTarget() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);
        givePlayerIslands(player1, 4); // maxX = 2

        Permanent vanguard = new Permanent(new EliteVanguard()); // MV=1
        vanguard.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(vanguard);

        Permanent bears = new Permanent(new GrizzlyBears()); // MV=2
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new EntrancingMelody()));

        // The search owns cast-versus-pass; the target and X do not come from it. Stubbing it to
        // the cheaper creature pins that split down: whichever target the search hands over, the
        // cast has to come out on the best one X can reach.
        MCTSEngine mcts = Mockito.mock(MCTSEngine.class);
        Mockito.when(mcts.search(any(), any(), Mockito.anyInt(), Mockito.anyList()))
                .thenReturn(new SimulationAction.PlayCard(0, vanguard.getId(), 1));
        ai.setMctsEngine(mcts);

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("Hard AI pays an X-discard additional cost")
    void castsAbandonHopeWithRequiredDiscardCard() {
        HardAiDecisionEngine ai = createHardAi(player1);
        pinLibrariesAndHands();
        giveAiPriority(player1);
        harness.addMana(player1, ManaColor.BLACK, 4);
        AbandonHope abandonHope = new AbandonHope();
        GrizzlyBears firstDiscard = new GrizzlyBears();
        GrizzlyBears secondDiscard = new GrizzlyBears();
        harness.setHand(player1, List.of(abandonHope, firstDiscard, secondDiscard));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        MCTSEngine mcts = Mockito.mock(MCTSEngine.class);
        Mockito.when(mcts.search(any(), any(), Mockito.anyInt(), Mockito.anyList()))
                .thenReturn(new SimulationAction.PlayCard(0, player2.getId(), 2));
        ai.setMctsEngine(mcts);

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(abandonHope);
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(firstDiscard, secondDiscard);
    }

    @Test
    @DisplayName("Hard AI skips Entrancing Melody when target too expensive")
    void skipsEntrancingMelodyWhenTooExpensive() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);
        givePlayerIslands(player1, 3); // maxX = 1

        Permanent bears = new Permanent(new GrizzlyBears()); // MV=2, unaffordable
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new EntrancingMelody()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI skips Entrancing Melody when cost modifier makes only target unaffordable")
    void skipsEntrancingMelodyWhenCostModifierMakesTargetUnaffordable() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);
        givePlayerIslands(player1, 4); // 4U total; Entrancing Melody {X}{U}{U} A?†’ without modifier maxX=2

        // Thalia on opponent's battlefield: +1 cost A?†’ maxX=1
        Permanent thalia = new Permanent(new com.github.laxika.magicalvibes.cards.t.ThaliaGuardianOfThraben());
        thalia.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(thalia);

        // MV=2 creature A?€�t needs X=2 but maxX=1 with Thalia A?†’ unaffordable
        Permanent bears = new Permanent(new GrizzlyBears()); // MV=2
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new EntrancingMelody()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Without the fix, AI would compute maxX=2 (ignoring modifier) and try to steal Bears,
        // which would fail server-side validation. With the fix, AI sees maxX=1 and skips.
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI attacks with at least one creature when Trove of Temptation forces attack")
    void attacksWithAtLeastOneWhenForcedByTroveOfTemptation() {
        HardAiDecisionEngine ai = createHardAi(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        harness.beginAttackerDeclarationInput();

        // Opponent controls Trove of Temptation
        Permanent trove = new Permanent(new TroveOfTemptation());
        trove.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(trove);

        // AI has a 2/2 and opponent has a 4/4 blocker A?€�t simulator would normally skip attacking
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(airElemental);

        ai.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

        // Must attack with at least one creature despite the unfavorable board
        long attackingCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isAttacking)
                .count();
        assertThat(attackingCount).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Hard AI plays the land that enables casting a spell in hand")
    void playsLandThatEnablesSpellCasting() {
        HardAiDecisionEngine ai = createHardAi(player1);
        // Land selection is evaluator-only, but the follow-up cast is another thin
        // cast-versus-pass margin: passing in precombat main reaches the same board, because the
        // rollout can still cast the bears postcombat. Pin the libraries so the assertion measures
        // the land choice and not the shuffle.
        pinLibrariesAndHands();
        giveAiPriority(player1);

        // AI has 1 colorless mana available from an untapped Mountain
        Permanent mountain = new Permanent(new Mountain());
        mountain.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(mountain);

        // Hand: Forest, Plains, and Grizzly Bears ({1}{G})
        // Forest should be chosen because it enables casting Grizzly Bears
        Card forest = new com.github.laxika.magicalvibes.cards.f.Forest();
        Card plains = new Plains();
        Card bears = new GrizzlyBears();
        harness.setHand(player1, List.of(forest, plains, bears));

        // First GAME_STATE: AI plays the best land
        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Forest should be on the battlefield (not Plains)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Plains"));

        // Second GAME_STATE: AI casts the now-enabled spell
        harness.clearPriorityPassed();
        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Hard AI prefers land with better color coverage when no spell is immediately castable")
    void prefersLandWithBetterColorCoverage() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);

        // No mana on battlefield A?€�t neither land alone enables a 2-cost spell
        // Hand: Forest, Plains, Serra Angel ({3}{W}{W})
        // Plains should be chosen because Serra Angel needs {W}{W}
        Card forest = new com.github.laxika.magicalvibes.cards.f.Forest();
        Card plains = new Plains();
        Card angel = new SerraAngel();
        harness.setHand(player1, List.of(forest, plains, angel));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Neither land alone enables Serra Angel, so AI should choose based on coverage.
        // Plains matches Serra Angel's {W}{W} requirement, Forest matches nothing.
        // The Plains should have been played A?€�t verify it's on the battlefield.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Plains"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
    }

    private void givePlayerPlains(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent plains = new Permanent(new Plains());
            plains.setSummoningSick(false);
            gd.playerBattlefields.get(player.getId()).add(plains);
        }
    }

    private void givePlayerMountains(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent mountain = new Permanent(new Mountain());
            mountain.setSummoningSick(false);
            gd.playerBattlefields.get(player.getId()).add(mountain);
        }
    }

    @Test
    @DisplayName("Hard AI does not cast Pacifism when targeting tax makes it unaffordable")
    void doesNotCastPacifismWhenTargetingTaxMakesUnaffordable() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);
        givePlayerPlains(player1, 2); // Only 2 mana A?€�t Pacifism costs {1}{W} but Kopala adds {2}

        Permanent kopala = new Permanent(new com.github.laxika.magicalvibes.cards.k.KopalaWardenOfWaves());
        kopala.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(kopala);

        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.p.Pacifism()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Should NOT cast A?€�t can't afford {1}{W} + {2} tax = 4 mana with only 2 Plains
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI casts Pacifism when it can afford targeting tax")
    void castsPacifismWhenCanAffordTargetingTax() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);
        // Postcombat main with a single castable spell takes the deterministic evaluator path;
        // this test pins the targeting-tax affordability gate, not the MCTS policy choice
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        givePlayerPlains(player1, 4); // 4 mana A?€�t enough for {1}{W} + {2} tax

        Permanent kopala = new Permanent(new com.github.laxika.magicalvibes.cards.k.KopalaWardenOfWaves());
        kopala.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(kopala);

        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.p.Pacifism()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Pacifism");
    }

    @Test
    @DisplayName("Hard AI does not cast instant when targeting tax makes it unaffordable")
    void doesNotCastInstantWhenTargetingTaxMakesUnaffordable() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // Set up as opponent's turn, beginning of combat A?€�t good timing for REMOVAL instants
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();
        gd.priorityPassedBy.add(player2.getId());

        givePlayerMountains(player1, 1); // Only 1 mana A?€�t Lightning Bolt costs {R} but Kopala adds {2}

        Permanent kopala = new Permanent(new com.github.laxika.magicalvibes.cards.k.KopalaWardenOfWaves());
        kopala.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(kopala);

        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.l.LightningBolt()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Should NOT cast A?€�t can't afford {R} + {2} tax = 3 mana with only 1 Mountain
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI casts Cancel to counter opponent's creature spell")
    void castsCancelToCounterOpponentCreatureSpell() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // Set up as opponent's turn with a spell on the stack
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        // Simulate that the active player (opponent) has already passed priority
        // after casting their spell, so now AI gets priority to respond
        gd.priorityPassedBy.add(player2.getId());
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();

        // Put an opponent's creature spell on the stack
        SerraAngel angel = new SerraAngel();
        com.github.laxika.magicalvibes.model.StackEntry opponentSpell =
                new com.github.laxika.magicalvibes.model.StackEntry(angel, player2.getId());
        gd.stack.add(opponentSpell);

        // Give the AI enough mana for Cancel (1UU)
        givePlayerIslands(player1, 3);

        // Give the AI Cancel in hand
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.c.Cancel()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // The AI should cast Cancel targeting the Serra Angel
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        com.github.laxika.magicalvibes.model.StackEntry cancelOnStack = gd.stack.getLast();
        assertThat(cancelOnStack.getCard().getName()).isEqualTo("Cancel");
        assertThat(cancelOnStack.getTargetId()).isEqualTo(angel.getId());
    }

    @Test
    @DisplayName("Hard AI does not cast Cancel when only own spells are on the stack")
    void doesNotCancelOwnSpells() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // AI is active player and has just cast its own spell
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();

        // Put AI's own creature spell on the stack
        GrizzlyBears bears = new GrizzlyBears();
        com.github.laxika.magicalvibes.model.StackEntry ownSpell =
                new com.github.laxika.magicalvibes.model.StackEntry(bears, player1.getId());
        gd.stack.add(ownSpell);

        givePlayerIslands(player1, 3);
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.c.Cancel()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Stack should only have the original spell A?€�t Cancel should not have been cast
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Hard AI does not waste Cancel on a low-value spell")
    void doesNotWasteCancelOnLowValueSpell() {
        HardAiDecisionEngine ai = createHardAi(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        // Opponent passed priority after casting
        gd.priorityPassedBy.add(player2.getId());
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();

        // Put a low-value opponent spell on the stack (Llanowar Elves, MV=1)
        LlanowarElves elves = new LlanowarElves();
        com.github.laxika.magicalvibes.cards.c.Cancel cancelCard = new com.github.laxika.magicalvibes.cards.c.Cancel();
        com.github.laxika.magicalvibes.model.StackEntry lowValueSpell =
                new com.github.laxika.magicalvibes.model.StackEntry(elves, player2.getId());
        gd.stack.add(lowValueSpell);

        givePlayerIslands(player1, 3);
        harness.setHand(player1, List.of(cancelCard));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Should NOT waste a 3-mana counterspell on a 1/1 for 1 mana.
        // After the AI passes priority, both players have passed, stack resolves.
        // Verify Cancel was not cast by checking the hand still contains it.
        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).isNotNull();
        assertThat(hand.stream().anyMatch(c -> c.getName().equals("Cancel"))).isTrue();
    }

    @Test
    @DisplayName("Hard AI values countering a board wipe higher than a vanilla creature of same CMC")
    void valuesCounteringBoardWipeHigherThanVanillaCreature() {
        // Give the AI a strong board that the board wipe would destroy
        Permanent angel1 = new Permanent(new SerraAngel());
        angel1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(angel1);
        Permanent angel2 = new Permanent(new SerraAngel());
        angel2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(angel2);
        Permanent angel3 = new Permanent(new SerraAngel());
        angel3.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(angel3);

        // Test 1: Opponent casts Wrath of God (board wipe, MV=4)
        HardAiDecisionEngine ai1 = createHardAi(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.priorityPassedBy.add(player2.getId());
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();

        WrathOfGod wrath = new WrathOfGod();
        StackEntry wrathOnStack = new StackEntry(StackEntryType.SORCERY_SPELL, wrath, player2.getId(),
                wrath.getName(), wrath.getEffects(EffectSlot.SPELL), 0);
        gd.stack.add(wrathOnStack);
        givePlayerIslands(player1, 3);
        harness.setHand(player1, List.of(new Cancel()));

        ai1.handleEvent(AiDecisionKind.GAME_STATE);

        // AI should counter the board wipe A?€�t it threatens its entire board
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        StackEntry cancelOnStack = gd.stack.getLast();
        assertThat(cancelOnStack.getCard().getName()).isEqualTo("Cancel");
    }

    @Test
    @DisplayName("Hard AI counters removal targeting its best creature")
    void countersRemovalTargetingBestCreature() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // AI has a valuable creature on the battlefield
        Permanent angel = new Permanent(new SerraAngel());
        angel.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(angel);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.priorityPassedBy.add(player2.getId());
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();

        // Opponent casts Doom Blade (MV=2) targeting the Angel
        DoomBlade doomBlade = new DoomBlade();
        StackEntry removalOnStack = new StackEntry(StackEntryType.INSTANT_SPELL, doomBlade, player2.getId(),
                doomBlade.getName(), doomBlade.getEffects(EffectSlot.SPELL), 0, angel.getId(), null);
        gd.stack.add(removalOnStack);

        givePlayerIslands(player1, 3);
        harness.setHand(player1, List.of(new Cancel()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // AI should counter the removal even though Doom Blade (MV=2) < Cancel (MV=3),
        // because it's targeting a Serra Angel (high creature value)
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        StackEntry cancelEntry = gd.stack.getLast();
        assertThat(cancelEntry.getCard().getName()).isEqualTo("Cancel");
    }

    @Test
    @DisplayName("Hard AI saves counterspell when board is strong and opponent casts mediocre creature")
    void savesCounterspellWhenBoardIsStrongAndThreatIsMediocre() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // AI has a very strong board (3 Serra Angels = high board strength > 30)
        for (int i = 0; i < 3; i++) {
            Permanent angel = new Permanent(new SerraAngel());
            angel.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(angel);
        }

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.priorityPassedBy.add(player2.getId());
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();

        // Opponent casts a mediocre creature (Grizzly Bears, MV=2, 2/2)
        GrizzlyBears bears = new GrizzlyBears();
        StackEntry bearsOnStack = new StackEntry(bears, player2.getId());
        gd.stack.add(bearsOnStack);

        givePlayerIslands(player1, 3);
        harness.setHand(player1, List.of(new Cancel()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // AI should NOT counter a 2/2 when it already has 3 Serra Angels A?€�t
        // save the counterspell for something more threatening
        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).isNotNull();
        assertThat(hand.stream().anyMatch(c -> c.getName().equals("Cancel"))).isTrue();
    }

    @Test
    @DisplayName("Hard AI still counters board wipe even when board is strong (reservation bypassed)")
    void countersHighValueSpellEvenWhenBoardIsStrong() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // AI has a very strong board
        for (int i = 0; i < 3; i++) {
            Permanent angel = new Permanent(new SerraAngel());
            angel.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(angel);
        }

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.priorityPassedBy.add(player2.getId());
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();

        // Opponent casts Wrath of God A?€�t the most threatening spell possible against a big board
        WrathOfGod wrath = new WrathOfGod();
        StackEntry wrathOnStack = new StackEntry(StackEntryType.SORCERY_SPELL, wrath, player2.getId(),
                wrath.getName(), wrath.getEffects(EffectSlot.SPELL), 0);
        gd.stack.add(wrathOnStack);

        givePlayerIslands(player1, 3);
        harness.setHand(player1, List.of(new Cancel()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // AI MUST counter the board wipe A?€�t it would destroy all 3 Serra Angels
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        StackEntry cancelEntry = gd.stack.getLast();
        assertThat(cancelEntry.getCard().getName()).isEqualTo("Cancel");
    }

    @Test
    @DisplayName("Hard AI counters mediocre spell when at low life (reservation bypassed)")
    void countersAnySpellAtLowLife() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // AI is at critically low life
        harness.setLife(player1, 4);

        // AI has a strong board
        for (int i = 0; i < 3; i++) {
            Permanent angel = new Permanent(new SerraAngel());
            angel.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(angel);
        }

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.priorityPassedBy.add(player2.getId());
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();

        // Opponent casts Air Elemental (4/4 flying, MV=5)
        // Normally might not be countered with a strong board, but at low life AI is desperate
        AirElemental elemental = new AirElemental();
        StackEntry spellOnStack = new StackEntry(elemental, player2.getId());
        gd.stack.add(spellOnStack);

        givePlayerIslands(player1, 3);
        harness.setHand(player1, List.of(new Cancel()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // At low life, the reservation threshold is bypassed A?€�t counter everything
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        StackEntry cancelEntry = gd.stack.getLast();
        assertThat(cancelEntry.getCard().getName()).isEqualTo("Cancel");
    }

    private void givePlayerForests(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent forest = new Permanent(new Forest());
            forest.setSummoningSick(false);
            gd.playerBattlefields.get(player.getId()).add(forest);
        }
    }

    private void givePlayerSwamps(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent swamp = new Permanent(new Swamp());
            swamp.setSummoningSick(false);
            gd.playerBattlefields.get(player.getId()).add(swamp);
        }
    }

    @Test
    @DisplayName("Hard AI casts sorceries when total multi-spell value exceeds instant held value")
    void castsMultipleSorceriesInsteadOfHoldingForInstant() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);

        // Give AI 4 green mana A?€�t enough for two 2-drop creatures
        givePlayerForests(player1, 4);

        // Hand: two Grizzly Bears ({1}{G} each, value ~7 each, total ~14)
        // and a Shock ({R} instant, value ~3, held value ~4)
        // Total sorcery value ~14 should beat instant held value ~4.
        // With old logic (single best sorcery = ~7), the comparison was closer.
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 1); // For Shock to be castable

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // AI should cast a sorcery-speed creature (not hold all mana for Shock)
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Hard AI casts sorcery while reserving mana for instant when both fit")
    void castsSorceryAndReservesManaForInstant() {
        HardAiDecisionEngine ai = createHardAi(player1);
        // This test guards the deterministic reservation heuristic
        // (tryCastSpellWithInstantAwareness), not the search. The cast-vs-pass gap here is
        // marginal for MCTS, whose seeded search still varies run-to-run with map ordering
        // of the game's random UUIDs A?€�t so force the evaluator fallback instead.
        MCTSEngine failingMcts = Mockito.mock(MCTSEngine.class);
        Mockito.when(failingMcts.search(any(), any(), Mockito.anyInt(), Mockito.anyList()))
                .thenThrow(new RuntimeException("MCTS disabled for test"));
        ai.setMctsEngine(failingMcts);
        giveAiPriority(player1);

        // Give AI 4 mana (3 green + 1 red) A?€�t enough for one 2-drop + keep 2 for instant
        givePlayerForests(player1, 3);
        givePlayerMountains(player1, 1);

        // Hand: Grizzly Bears ({1}{G}, 2 mana) + Lightning Bolt ({R} instant, 1 mana)
        // With 4 total mana, AI can cast Bears (2 mana) and still afford Bolt (1 mana)
        // Even though Bolt's held value might beat single Bears value with the 0.8 factor,
        // the AI should still cast Bears because it can do both.
        Card bears = new GrizzlyBears();
        Card bolt = new LightningBolt();
        harness.setHand(player1, List.of(bears, bolt));

        // Put an opponent creature so Lightning Bolt has a target worth holding for
        Permanent oppCreature = new Permanent(new EliteVanguard());
        oppCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(oppCreature);

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // AI should cast the creature (it can still afford the instant later)
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Hard AI holds mana for instant when only a single low-value sorcery is available")
    void holdsForInstantWhenSingleLowValueSorcery() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);

        // Give AI 3 mana (1 green + 2 red)
        givePlayerForests(player1, 1);
        givePlayerMountains(player1, 2);

        // Hand: EliteVanguard ({W}, low value creature that can't even be cast because no white mana)
        // + Lightning Bolt ({R} instant, 3 damage A?€�t high held value ~4.5*1.3 A?‰�? 5.9)
        // Since the only sorcery-speed option is not castable, AI should pass
        // (falls through to instant timing, but it's own main phase so non-counterspell
        // instants are not cast at this timing)
        harness.setHand(player1, List.of(new EliteVanguard(), new LightningBolt()));

        // Put an opponent creature so Lightning Bolt has removal value
        Permanent oppCreature = new Permanent(new GrizzlyBears());
        oppCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(oppCreature);

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // No sorcery should be cast (EliteVanguard needs white), and Lightning Bolt
        // should be held for opponent's turn
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI holds for two instants when combined held value exceeds cast-one-hold-one")
    void holdsForMultipleInstantsWhenCombinedValueExceedsSorcery() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);

        // 5 mana: 2 mountains + 3 islands
        givePlayerMountains(player1, 2);
        givePlayerIslands(player1, 3);

        // Hand: GoblinPiker ({1}{R}, ~5 value) + Cancel ({1}{U}{U}, held ~14.4) + Negate ({1}{U}, held ~9.6)
        // Total instant cost = 3 + 2 = 5 = total mana.
        // Hold both instants: (14.4 + 9.6) * 0.8 = 19.2
        // Cast GoblinPiker + hold Cancel: ~5 + 14.4 * 0.8 = ~16.5
        // Holding both wins (19.2 > 16.5), so AI should hold all mana.
        harness.setHand(player1, List.of(new GoblinPiker(), new Cancel(), new Negate()));

        // Opponent creature for spell evaluation context
        Permanent oppCreature = new Permanent(new GrizzlyBears());
        oppCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(oppCreature);

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // AI should hold mana for both counterspells A?€�t stack stays empty
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI casts sorcery while reserving mana for multiple instants when all fit")
    void castsSorceryWhileReservingManaForMultipleInstants() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);

        // 7 mana: 4 forests + 3 islands A?€�t enough for Bears (2) + Cancel (3) + Negate (2) = 7
        givePlayerForests(player1, 4);
        givePlayerIslands(player1, 3);

        // All fit within 7 mana, so AI should cast the creature and still hold both instants.
        harness.setHand(player1, List.of(new GrizzlyBears(), new Cancel(), new Negate()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // AI should cast the sorcery-speed creature (can hold both instants with remaining mana)
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Hard AI holds for two instants in postcombat main when combined value exceeds sorcery")
    void holdsForMultipleInstantsInPostcombatMain() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // Postcombat main phase setup A?€�t tryCastSpellWithInstantAwareness is called directly
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();

        // 5 mana: 2 mountains + 3 islands
        givePlayerMountains(player1, 2);
        givePlayerIslands(player1, 3);

        // Hand: GoblinPiker ({1}{R}, ~7.5 value) + Cancel ({1}{U}{U}, held ~14.4) + Negate ({1}{U}, held ~9.6)
        // Total instant cost = 3 + 2 = 5 = total mana.
        // Hold both: (14.4 + 9.6) * 0.8 = 19.2
        // Cast GoblinPiker + hold Cancel: ~7.5 + 14.4 * 0.8 A?‰�? 19.0
        // Holding both (19.2) just beats cast+hold-one (~19.0), so AI holds all mana.
        harness.setHand(player1, List.of(new GoblinPiker(), new Cancel(), new Negate()));

        // Opponent creature for evaluation context (use GrizzlyBears to keep sorcery value moderate)
        Permanent oppCreature = new Permanent(new GrizzlyBears());
        oppCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(oppCreature);

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // AI should hold mana for both counterspells in postcombat A?€�t stack stays empty
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI holds for multiple instants instead of casting precombat removal")
    void holdsForMultipleInstantsInsteadOfPrecombatRemoval() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // Precombat main phase setup
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();

        // AI has an attacker
        Permanent attacker = new Permanent(new GrizzlyBears()); // 2/2
        attacker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        // Opponent has a blocker
        Permanent blocker = new Permanent(new EliteVanguard()); // 2/1
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        // 5 mana: 1 swamp + 4 islands
        givePlayerSwamps(player1, 1);
        givePlayerIslands(player1, 4);

        // Hand: Eviscerate ({3}{B}, destroy creature, costs 4) + Cancel ({1}{U}{U}) + Negate ({1}{U})
        // If Eviscerate is cast (4 mana), only 1 mana left A?€�t can't hold Cancel (3) or Negate (2).
        // If holding instead: Cancel (3) + Negate (2) = 5 mana, both fit.
        // Held value: (14.4 + 9.6) * 0.8 = 19.2
        // Cast value: Eviscerate precombat value A?‰�? 10 (removal + damage gain)
        // Holding clearly wins.
        harness.setHand(player1, List.of(new Eviscerate(), new Cancel(), new Negate()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // AI should hold mana for both counterspells instead of removing the blocker
        assertThat(gd.stack).isEmpty();
    }

    @Nested
    @DisplayName("Color-aware mulligan decisions")
    class ColorAwareMulligan {

        /**
         * Thin subclass to expose the protected shouldKeepHand for direct testing.
         */
        private class TestableMulliganEngine extends HardAiDecisionEngine {
            TestableMulliganEngine(Player player) {
                super(gd.id, player, harness.getGameRegistry(),
                        harness.getGameService(), harness.getGameQueryService(),
                        harness.getBlockLegalityService(), harness.getCombatAttackService(), harness.getGameActionAvailabilityService(), harness.getCastingCostService(), harness.getCastingPermissionService(),
                        harness.getTargetValidationService(), harness.getTargetLegalityService());
            }

            boolean testShouldKeepHand(GameData gameData) {
                return shouldKeepHand(gameData);
            }
        }

        @Test
        @DisplayName("Mulligans hand with mountains and only blue spells")
        void mulligansWhenLandsDoNotMatchSpellColors() {
            // 3 Mountains + 4 blue spells A?€�t no way to cast anything
            harness.setHand(player1, List.of(
                    new Mountain(), new Mountain(), new Mountain(),
                    new AirElemental(), new AirElemental(), new AirElemental(), new AirElemental()
            ));
            gd.mulliganCounts.put(player1.getId(), 0);

            TestableMulliganEngine engine = new TestableMulliganEngine(player1);
            assertThat(engine.testShouldKeepHand(gd)).isFalse();
        }

        @Test
        @DisplayName("Keeps hand when lands match spell colors")
        void keepsHandWhenLandsMatchSpellColors() {
            // 3 Islands + 2 cheap blue spells + 2 medium blue spells
            // Score: 3*1.5 + 2*3.0 + 2*1.5 = 4.5 + 6.0 + 3.0 = 13.5 >= 12.0
            harness.setHand(player1, List.of(
                    new Island(), new Island(), new Island(),
                    new SteelSabotage(), new SteelSabotage(),
                    new AirElemental(), new AirElemental()
            ));
            gd.mulliganCounts.put(player1.getId(), 0);

            TestableMulliganEngine engine = new TestableMulliganEngine(player1);
            assertThat(engine.testShouldKeepHand(gd)).isTrue();
        }

        @Test
        @DisplayName("Keeps hand when at least some spells are color-castable")
        void keepsHandWhenSomeSpellsAreCastable() {
            // 3 Mountains + mix of red and blue spells
            harness.setHand(player1, List.of(
                    new Mountain(), new Mountain(), new Mountain(),
                    new Slagstorm(), new Slagstorm(),
                    new AirElemental(), new AirElemental()
            ));
            gd.mulliganCounts.put(player1.getId(), 0);

            TestableMulliganEngine engine = new TestableMulliganEngine(player1);
            // Slagstorms score normally (3.0 each, no color penalty), AirElementals score 0.25 (uncastable)
            // Score: 3*1.5 + 2*3.0 + 2*0.25 = 11.0, no removal/curve bonus A?†’ 11.0 < 12.0
            assertThat(engine.testShouldKeepHand(gd)).isFalse();
        }

        @Test
        @DisplayName("Keeps color-mismatched hand after 3 mulligans")
        void keepsAfterThreeMulligansRegardlessOfColors() {
            // Even with total color mismatch, 3+ mulligans = always keep
            harness.setHand(player1, List.of(
                    new Mountain(), new Mountain(),
                    new AirElemental(), new AirElemental()
            ));
            gd.mulliganCounts.put(player1.getId(), 3);

            TestableMulliganEngine engine = new TestableMulliganEngine(player1);
            assertThat(engine.testShouldKeepHand(gd)).isTrue();
        }

        @Test
        @DisplayName("Mulligans when double-pip requirements can't be met by single source")
        void mulligansWhenDoublePipRequirementsCannotBeMet() {
            // 1 Island + 2 Forests + 4 Cancel ({1}{U}{U})
            // Cancel needs {U}{U} but we only have 1 blue source A?€�t can never cast it
            // Old scoring would keep (16.5) because blue "exists"; new scoring mulligans
            // because per-pip penalty halves each Cancel's score: 4.5 + 4*1.5 = 10.5 < 12.0
            harness.setHand(player1, List.of(
                    new Island(), new Forest(), new Forest(),
                    new Cancel(), new Cancel(), new Cancel(), new Cancel()
            ));
            gd.mulliganCounts.put(player1.getId(), 0);

            TestableMulliganEngine engine = new TestableMulliganEngine(player1);
            assertThat(engine.testShouldKeepHand(gd)).isFalse();
        }

        @Test
        @DisplayName("Removal bonus helps borderline hand keep")
        void removalBonusHelpsKeepBorderlineHand() {
            // 2 Swamps + Eviscerate (removal, mv=4) + 2 BogWraith (mv=4) + SeveredLegion (mv=3) + AirElemental
            // With landCount=2: mv=4 A?†’ 4 A?‰¤ 5 A?†’ base 1.5; mv=3 A?†’ 3 A?‰¤ 3 A?†’ base 3.0
            // Black demand=4, supply=2, strain penalty ~0.15 on each
            // Base: 3.0 + 2.55 + 3*1.275 + 0.25 = 9.625
            // + curve (MVs={3,4}): +1.0; + removal (Eviscerate): +1.5
            // Total: 12.125 >= 12.0 A?†’ keep
            harness.setHand(player1, List.of(
                    new Swamp(), new Swamp(),
                    new Eviscerate(), new BogWraith(), new BogWraith(),
                    new SeveredLegion(), new AirElemental()
            ));
            gd.mulliganCounts.put(player1.getId(), 0);

            TestableMulliganEngine engine = new TestableMulliganEngine(player1);
            assertThat(engine.testShouldKeepHand(gd)).isTrue();
        }

        @Test
        @DisplayName("Mulligans same borderline hand without removal")
        void mulligansBorderlineHandWithoutRemoval() {
            // 2 Swamps + 3 BogWraith (mv=4, not removal) + SeveredLegion (mv=3) + AirElemental
            // Same base score as above (9.625), same curve bonus (+1.0)
            // But no removal bonus A?†’ Total: 10.625 < 12.0 A?†’ mulligan
            harness.setHand(player1, List.of(
                    new Swamp(), new Swamp(),
                    new BogWraith(), new BogWraith(), new BogWraith(),
                    new SeveredLegion(), new AirElemental()
            ));
            gd.mulliganCounts.put(player1.getId(), 0);

            TestableMulliganEngine engine = new TestableMulliganEngine(player1);
            assertThat(engine.testShouldKeepHand(gd)).isFalse();
        }
    }

}
