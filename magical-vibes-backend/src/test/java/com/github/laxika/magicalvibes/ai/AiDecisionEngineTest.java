package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AiDecisionEngineTest {

    private GameTestHarness harness;
    private Player human;
    private Player aiPlayer;
    private GameService gs;
    private GameData gd;
    private AiDecisionEngine ai;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        human = harness.getPlayer1();
        aiPlayer = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();

        ai = new AiDecisionEngine(gd.id, aiPlayer, harness.getGameRegistry(), gs);
    }

    /**
     * Sets up the game so the AI (player2) has priority in precombat main with an empty stack.
     */
    private void giveAiPriority() {
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.awaitingInput = null;
        gd.stack.clear();
    }

    /**
     * Adds the given number of untapped Plains to the AI's battlefield.
     */
    private void giveAiPlains(int count) {
        for (int i = 0; i < count; i++) {
            Permanent plains = new Permanent(new Plains());
            plains.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(plains);
        }
    }

    // ===== Detrimental aura targeting =====

    @Test
    @DisplayName("AI casts Pacifism on opponent's highest-power creature")
    void castsPacifismOnHighestPowerCreature() {
        giveAiPriority();
        giveAiPlains(2);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(airElemental);

        harness.setHand(aiPlayer, List.of(new Pacifism()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Pacifism");
        assertThat(gd.stack.getFirst().getTargetPermanentId()).isEqualTo(airElemental.getId());
    }

    @Test
    @DisplayName("AI does not cast second Pacifism on already-pacified creature")
    void doesNotDoublePacify() {
        giveAiPriority();
        giveAiPlains(2);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(airElemental);

        // Air Elemental already has Pacifism attached
        Permanent existingPacifism = new Permanent(new Pacifism());
        existingPacifism.setAttachedTo(airElemental.getId());
        gd.playerBattlefields.get(aiPlayer.getId()).add(existingPacifism);

        harness.setHand(aiPlayer, List.of(new Pacifism()));

        ai.handleMessage("GAME_STATE", "");

        // AI should cast on the Grizzly Bears instead
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Pacifism");
        assertThat(gd.stack.getFirst().getTargetPermanentId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("AI does not cast Pacifism when all opponent creatures already have one")
    void doesNotCastWhenAllCreaturesPacified() {
        giveAiPriority();
        giveAiPlains(2);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        // Bears already has Pacifism
        Permanent existingPacifism = new Permanent(new Pacifism());
        existingPacifism.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(aiPlayer.getId()).add(existingPacifism);

        harness.setHand(aiPlayer, List.of(new Pacifism()));

        ai.handleMessage("GAME_STATE", "");

        // AI should not cast — no valid targets, so it passes priority instead
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("AI casts Pacifism when opponent has mix of pacified and unpacified creatures")
    void castsOnUnpacifiedAmongMixed() {
        giveAiPriority();
        giveAiPlains(2);

        Permanent bears1 = new Permanent(new GrizzlyBears());
        bears1.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears1);

        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears2.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears2);

        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(airElemental);

        // Air Elemental already pacified
        Permanent existingPacifism = new Permanent(new Pacifism());
        existingPacifism.setAttachedTo(airElemental.getId());
        gd.playerBattlefields.get(aiPlayer.getId()).add(existingPacifism);

        harness.setHand(aiPlayer, List.of(new Pacifism()));

        ai.handleMessage("GAME_STATE", "");

        // Should target one of the unpacified bears (both 2/2, either is valid)
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetPermanentId())
                .isIn(bears1.getId(), bears2.getId());
    }

    // ===== Beneficial aura targeting =====

    @Test
    @DisplayName("AI casts beneficial aura on own creature")
    void castsBeneficialAuraOnOwnCreature() {
        giveAiPriority();

        // Holy Strength costs {W}
        Permanent plains = new Permanent(new Plains());
        plains.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(plains);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(bears);

        harness.setHand(aiPlayer, List.of(new HolyStrength()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Holy Strength");
        assertThat(gd.stack.getFirst().getTargetPermanentId()).isEqualTo(bears.getId());
    }

    // ===== No target available =====

    @Test
    @DisplayName("AI does not cast Pacifism when opponent has no creatures")
    void doesNotCastWithoutOpponentCreatures() {
        giveAiPriority();
        giveAiPlains(2);

        // Opponent has no creatures
        harness.setHand(aiPlayer, List.of(new Pacifism()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).isEmpty();
    }
}
