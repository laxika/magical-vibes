package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.p.PaleBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.effect.normalfx.D20RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollTwoD20IgnoreLowerEffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BerserkersFrenzy.class, PaleBears.class})
class BerserkersFrenzyTest extends BaseCardTest {

    private RollTwoD20IgnoreLowerEffectHandler rollHandler;
    private D20RollService originalD20RollService;

    @BeforeEach
    void captureD20RollService() {
        rollHandler = GameTestEngineContext.get().getBean(RollTwoD20IgnoreLowerEffectHandler.class);
        originalD20RollService = (D20RollService) ReflectionTestUtils.getField(rollHandler, "d20RollService");
    }

    @AfterEach
    void restoreD20RollService() {
        ReflectionTestUtils.setField(rollHandler, "d20RollService", originalD20RollService);
    }

    @Test
    void lowerResultLetsControllerChooseCreaturesThatMustBlock() {
        ReflectionTestUtils.setField(rollHandler, "d20RollService", new FixedD20RollService(4, 14));
        Permanent attacker = addAttacker();
        Permanent blocker = addCreatureReady(player2, new PaleBears());
        castDuringDeclareAttackers();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(attacker.getId(), blocker.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(blocker.getId()));
        assertThat(blocker.isMustBlockThisTurnIfAble()).isTrue();

        advanceToBlockerDeclaration();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void higherResultLetsControllerChooseTheBlockAssignments() {
        ReflectionTestUtils.setField(rollHandler, "d20RollService", new FixedD20RollService(14, 15));
        Permanent attacker = addAttacker();
        Permanent blocker = addCreatureReady(player2, new PaleBears());
        castDuringDeclareAttackers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        advanceToBlockerDeclaration();

        PendingInteraction.BlockerDeclaration pending =
                gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class);
        assertThat(pending).isNotNull();
        assertThat(pending.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(pending.choosingForOpponent()).isTrue();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBeCastBeforeCombatButNotOnceBlockersAreBeingDeclared() {
        ReflectionTestUtils.setField(rollHandler, "d20RollService", new FixedD20RollService(1, 2));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BerserkersFrenzy()));
        addMana();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Berserker's Frenzy"));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BerserkersFrenzy()));
        addMana();
        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new PaleBears());
        attacker.setAttacking(true);
        attacker.tap();
        return attacker;
    }

    private void castDuringDeclareAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BerserkersFrenzy()));
        addMana();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void advanceToBlockerDeclaration() {
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private static final class FixedD20RollService extends D20RollService {

        private final int[] results;
        private int index;

        private FixedD20RollService(int... results) {
            this.results = results;
        }

        @Override
        public int roll() {
            return results[Math.min(index++, results.length - 1)];
        }
    }
}
