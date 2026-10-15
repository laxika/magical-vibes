package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.MillennialGargoyle;
import com.github.laxika.magicalvibes.cards.s.Smite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuardianOfTheGateless.class, MillennialGargoyle.class, Smite.class})
class GuardianOfTheGatelessTest extends BaseCardTest {

    @Test
    @DisplayName("Guardian of the Gateless gets +1/+1 when it blocks one creature")
    void getsOnePlusOneWhenBlockingOneCreature() {
        Permanent guardian = addReadyGuardian(player2);
        addReadyAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(guardian.getPowerModifier()).isEqualTo(1);
        assertThat(guardian.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Guardian of the Gateless can block three creatures and triggers once")
    void blocksThreeCreaturesAndTriggersOnce() {
        Permanent guardian = addReadyGuardian(player2);
        addReadyAttacker(player1);
        addReadyAttacker(player1);
        addReadyAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1),
                new BlockerAssignment(0, 2)));

        assertThat(guardian.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(guardian.getPowerModifier()).isEqualTo(3);
        assertThat(guardian.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Not blocking does not trigger the boost")
    void doesNotTriggerWhenNotBlocking() {
        Permanent guardian = addReadyGuardian(player2);
        addReadyAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(guardian.getPowerModifier()).isZero();
        assertThat(guardian.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost lasts through end of combat and expires at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent guardian = addReadyGuardian(player2);
        addReadyAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(guardian.getPowerModifier()).isEqualTo(1);
        assertThat(guardian.getToughnessModifier()).isEqualTo(1);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(guardian.getPowerModifier()).isZero();
        assertThat(guardian.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost counts only creatures still being blocked when it resolves")
    void countsRemainingAttackersAtResolution() {
        Permanent guardian = addReadyGuardian(player2);
        Permanent removedAttacker = addReadyAttacker(player1);
        addReadyAttacker(player1);
        harness.setHand(player2, List.of(new Smite()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));
        harness.castInstant(player2, 0, removedAttacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(removedAttacker);
        harness.passBothPriorities();

        assertThat(guardian.getPowerModifier()).isEqualTo(1);
        assertThat(guardian.getToughnessModifier()).isEqualTo(1);
    }

    private Permanent addReadyGuardian(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GuardianOfTheGateless());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addReadyAttacker(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new MillennialGargoyle());
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        return permanent;
    }
}
