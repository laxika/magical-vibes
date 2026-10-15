package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyclaveSquid.class, Forest.class})
class SkyclaveSquidTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack without a landfall trigger")
    void cannotAttackWithoutLandfall() {
        Permanent squid = addCreatureReady(player1, new SkyclaveSquid());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(squid.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Can attack after a land you control enters")
    void canAttackAfterLandfall() {
        Permanent squid = addCreatureReady(player1, new SkyclaveSquid());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        declareAttackers(List.of(0));

        assertThat(squid.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Landfall attack permission wears off at end of turn")
    void attackPermissionWearsOffAtEndOfTurn() {
        Permanent squid = addCreatureReady(player1, new SkyclaveSquid());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(squid.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("An opponent's land does not trigger landfall")
    void opponentLandDoesNotTrigger() {
        Permanent squid = addCreatureReady(player1, new SkyclaveSquid());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(squid.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Landfall does not allow a summoning-sick Squid to attack")
    void landfallDoesNotOverrideSummoningSickness() {
        harness.addToBattlefield(player1, new SkyclaveSquid());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Landfall does not allow a tapped Squid to attack")
    void landfallDoesNotOverrideBeingTapped() {
        Permanent squid = addCreatureReady(player1, new SkyclaveSquid());
        squid.tap();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(squid.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("A Squid entering after landfall does not inherit another Squid's permission")
    void permissionAppliesOnlyToTheSquidThatTriggered() {
        Permanent originalSquid = addCreatureReady(player1, new SkyclaveSquid());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent laterSquid = addCreatureReady(player1, new SkyclaveSquid());

        assertThatThrownBy(() -> declareAttackers(List.of(2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(laterSquid.isAttacking()).isFalse();

        declareAttackers(List.of(0));
        assertThat(originalSquid.isAttacking()).isTrue();
    }
}
