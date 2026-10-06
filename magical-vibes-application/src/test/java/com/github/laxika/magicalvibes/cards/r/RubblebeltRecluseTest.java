package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({RubblebeltRecluse.class})
class RubblebeltRecluseTest extends BaseCardTest {

    @Test
    @DisplayName("Rubblebelt Recluse must attack each combat if able")
    void mustAttackWhenAble() {
        Permanent recluse = harness.addToBattlefieldAndReturn(player1, new RubblebeltRecluse());
        recluse.setSummoningSick(false);

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Declaring Rubblebelt Recluse as an attacker satisfies its requirement")
    void canAttackWhenAble() {
        Permanent recluse = harness.addToBattlefieldAndReturn(player1, new RubblebeltRecluse());
        recluse.setSummoningSick(false);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(recluse.isAttacking()).isTrue();
        assertThat(recluse.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped Rubblebelt Recluse need not attack")
    void needNotAttackWhenTapped() {
        Permanent recluse = harness.addToBattlefieldAndReturn(player1, new RubblebeltRecluse());
        recluse.setSummoningSick(false);
        recluse.setTapped(true);

        assertThatCode(() -> declareAttackers(List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A summoning-sick Rubblebelt Recluse need not attack")
    void needNotAttackWithSummoningSickness() {
        Permanent recluse = harness.addToBattlefieldAndReturn(player1, new RubblebeltRecluse());
        recluse.setSummoningSick(true);

        assertThatCode(() -> declareAttackers(List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("An opponent's Rubblebelt Recluse does not require the active player to attack")
    void opponentRecluseDoesNotForceAttack() {
        Permanent recluse = harness.addToBattlefieldAndReturn(player2, new RubblebeltRecluse());
        recluse.setSummoningSick(false);

        assertThatCode(() -> declareAttackers(player1, List.of())).doesNotThrowAnyException();
    }
}
