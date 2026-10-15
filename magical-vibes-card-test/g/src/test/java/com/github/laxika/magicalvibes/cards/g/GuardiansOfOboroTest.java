package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AncestralKatana;
import com.github.laxika.magicalvibes.cards.s.ShortCircuit;
import com.github.laxika.magicalvibes.cards.b.BambooGroveArcher;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuardiansOfOboro.class, BambooGroveArcher.class, ShortCircuit.class, AncestralKatana.class})
class GuardiansOfOboroTest extends BaseCardTest {

    @Test
    @DisplayName("An unmodified creature with defender cannot attack")
    void unmodifiedCreatureCannotAttack() {
        harness.addToBattlefield(player1, new GuardiansOfOboro());
        addCreatureReady(player1, new BambooGroveArcher());

        assertThatThrownBy(() -> declareAttackers(List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("A modified creature you control can attack as though it had no defender")
    void modifiedCreatureYouControlCanAttack() {
        harness.addToBattlefield(player1, new GuardiansOfOboro());
        Permanent wall = addCreatureReady(player1, new BambooGroveArcher());
        wall.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new BambooGroveArcher());

        declareAttackers(List.of(1));

        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("A modified creature controlled by an opponent cannot use Guardians of Oboro")
    void opponentModifiedCreatureCannotAttack() {
        harness.addToBattlefield(player1, new GuardiansOfOboro());
        Permanent wall = addCreatureReady(player2, new BambooGroveArcher());
        wall.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void modifiedGuardiansCanAttackUsingTheirOwnAbility() {
        Permanent guardians = addCreatureReady(player1, new GuardiansOfOboro());
        guardians.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(guardians.isAttacking()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void equipmentModifiesRegardlessOfItsController(boolean equipmentControlledByCreatureController) {
        Permanent guardians = addCreatureReady(player1, new GuardiansOfOboro());
        Permanent equipment = harness.addToBattlefieldAndReturn(
                equipmentControlledByCreatureController ? player1 : player2, new AncestralKatana());
        equipment.setAttachedTo(guardians.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(guardians.isAttacking()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void onlyAnAuraControlledByCreatureControllerModifies(boolean auraControlledByCreatureController) {
        Permanent guardians = addCreatureReady(player1, new GuardiansOfOboro());
        Permanent aura = harness.addToBattlefieldAndReturn(
                auraControlledByCreatureController ? player1 : player2, new ShortCircuit());
        aura.setAttachedTo(guardians.getId());

        if (auraControlledByCreatureController) {
            harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
            assertThat(guardians.isAttacking()).isTrue();
        } else {
            assertThatThrownBy(() -> declareAttackers(List.of(0)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid attacker index");
        }
    }

    @Test
    void removingTheLastCounterRemovesAttackPermission() {
        Permanent guardians = addCreatureReady(player1, new GuardiansOfOboro());
        guardians.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        guardians.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void attackPermissionDoesNotOverrideSummoningSickness() {
        Permanent guardians = harness.addToBattlefieldAndReturn(player1, new GuardiansOfOboro());
        guardians.setSummoningSick(true);
        guardians.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void attackPermissionDoesNotAllowTappedCreaturesToAttack() {
        Permanent guardians = addCreatureReady(player1, new GuardiansOfOboro());
        guardians.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        guardians.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }
}
