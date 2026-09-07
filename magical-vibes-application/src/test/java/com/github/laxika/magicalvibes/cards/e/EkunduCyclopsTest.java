package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.v.ViashinoWarrior;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EkunduCyclops.class, ViashinoWarrior.class})
class EkunduCyclopsTest extends BaseCardTest {

    @Test
    @DisplayName("Ekundu Cyclops may stay back when no other creature attacks")
    void notForcedWhenNobodyAttacks() {
        addCreatureReady(player1, new EkunduCyclops());
        addCreatureReady(player1, new ViashinoWarrior());

        assertThatCode(() -> declareAttackers(player1, List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Ekundu Cyclops must attack when another creature attacks")
    void forcedWhenAllyAttacks() {
        addCreatureReady(player1, new EkunduCyclops());
        addCreatureReady(player1, new ViashinoWarrior());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must also attack");
    }

    @Test
    @DisplayName("Ekundu Cyclops attacking alongside the other creature is legal")
    void attacksAlongsideAlly() {
        addCreatureReady(player1, new EkunduCyclops());
        addCreatureReady(player1, new ViashinoWarrior());

        assertThatCode(() -> declareAttackers(player1, List.of(0, 1))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Ekundu Cyclops may attack alone")
    void mayAttackAlone() {
        addCreatureReady(player1, new EkunduCyclops());
        addCreatureReady(player1, new ViashinoWarrior());

        assertThatCode(() -> declareAttackers(player1, List.of(0))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Ekundu Cyclops is not forced when it can't attack")
    void notForcedWhenUnableToAttack() {
        Permanent cyclops = addCreatureReady(player1, new EkunduCyclops());
        cyclops.tap();
        addCreatureReady(player1, new ViashinoWarrior());

        assertThatCode(() -> declareAttackers(player1, List.of(1))).doesNotThrowAnyException();
    }

    @Test
    @CardUsed(Errantry.class)
    @DisplayName("A can-only-attack-alone restriction makes the Cyclops unable to join an ally")
    void notForcedWhenCanOnlyAttackAlonePreventsJoining() {
        Permanent cyclops = addCreatureReady(player1, new EkunduCyclops());
        Permanent errantry = harness.addToBattlefieldAndReturn(player1, new Errantry());
        errantry.setAttachedTo(cyclops.getId());
        addCreatureReady(player1, new ViashinoWarrior());

        assertThatCode(() -> declareAttackers(player1, List.of(2))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("An opponent's attack does not force Ekundu Cyclops to attack")
    void notForcedByOpponentAttack() {
        addCreatureReady(player1, new ViashinoWarrior());
        addCreatureReady(player2, new EkunduCyclops());

        assertThatCode(() -> declareAttackers(player1, List.of(0))).doesNotThrowAnyException();
    }
}
