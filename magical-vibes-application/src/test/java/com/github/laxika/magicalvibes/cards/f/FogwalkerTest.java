package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.w.WoodlandPatrol;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Fogwalker.class, WoodlandPatrol.class})
class FogwalkerTest extends BaseCardTest {

    @Nested
    @DisplayName("ETB trigger")
    @CardUsed({Fogwalker.class, WoodlandPatrol.class})
    class EnterTheBattlefield {

        @Test
        @DisplayName("Makes target creature skip its controller's next untap step")
        void targetSkipsNextUntap() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new WoodlandPatrol());

            castFogwalker(player2, "Woodland Patrol");
            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(bears.getSkipUntapCount()).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new WoodlandPatrol());
        harness.addToBattlefield(player2, new WoodlandPatrol());
        UUID ownBearId = harness.getPermanentId(player1, "Woodland Patrol");
        harness.setHand(player1, List.of(new Fogwalker()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBearId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tappedTargetSkipsOnlyItsNextUntapStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WoodlandPatrol());
        target.setTapped(true);
        castFogwalker(player2, "Woodland Patrol");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void untappedTargetIsNotTappedAndRestrictionExpiresDuringNextUntapStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WoodlandPatrol());
        castFogwalker(player2, "Woodland Patrol");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        harness.performUntapStep(player2);
        target.setTapped(true);
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void overlappingTriggersDoNotPreventTwoUntapSteps() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WoodlandPatrol());
        target.setTapped(true);
        castFogwalker(player2, "Woodland Patrol");
        harness.passBothPriorities();
        harness.passBothPriorities();
        castFogwalker(player2, "Woodland Patrol");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void skulkPreventsGreaterPowerCreatureFromBlocking() {
        Permanent blocker = addCreatureReady(player2, new WoodlandPatrol());
        Permanent attacker = addCreatureReady(player1, new Fogwalker());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("skulk");
    }

    @Test
    void skulkAllowsEqualPowerCreatureToBlock() {
        Permanent blocker = addCreatureReady(player2, new Fogwalker());
        Permanent attacker = addCreatureReady(player1, new Fogwalker());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        assertThat(blocker.isBlocking()).isTrue();
    }

    private void castFogwalker(Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new Fogwalker()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
