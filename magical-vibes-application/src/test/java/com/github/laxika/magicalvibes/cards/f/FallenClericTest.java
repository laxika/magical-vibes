package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.Boneknitter;
import com.github.laxika.magicalvibes.cards.d.DaruLancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FallenCleric.class, Boneknitter.class, DaruLancer.class, Shock.class})
class FallenClericTest extends BaseCardTest {

    @Test
    @DisplayName("Cleric creature cannot block Fallen Cleric")
    void clericCreatureCannotBlock() {
        Permanent fallenCleric = addCreatureReady(player1, new FallenCleric());
        fallenCleric.setAttacking(true);

        addCreatureReady(player2, new Boneknitter());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Non-Cleric creature can block Fallen Cleric")
    void nonClericCreatureCanBlock() {
        Permanent fallenCleric = addCreatureReady(player1, new FallenCleric());
        fallenCleric.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new DaruLancer());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Fallen Cleric takes no combat damage from a Cleric")
    void takesNoCombatDamageFromCleric() {
        Permanent attacker = addCreatureReady(player1, new FallenCleric());
        attacker.setAttacking(true);

        Permanent fallenCleric = addCreatureReady(player2, new FallenCleric());
        fallenCleric.setBlocking(true);
        fallenCleric.addBlockingTarget(0);

        resolveCombat();

        assertThat(fallenCleric.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Fallen Cleric");
        harness.assertOnBattlefield(player2, "Fallen Cleric");
    }

    @Test
    @DisplayName("Cleric ability cannot target Fallen Cleric")
    void clericAbilityCannotTarget() {
        Permanent fallenCleric = addCreatureReady(player2, new FallenCleric());
        addCreatureReady(player1, new Boneknitter());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, fallenCleric.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Non-Cleric instant can target Fallen Cleric")
    void nonClericInstantCanTarget() {
        Permanent fallenCleric = addCreatureReady(player2, new FallenCleric());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, fallenCleric.getId());

        assertThat(fallenCleric.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can be cast face down and turned face up for its morph cost")
    void canBeCastFaceDownAndTurnedFaceUp() {
        harness.setHand(player1, List.of(new FallenCleric()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent fallenCleric = findPermanent(player1, "Fallen Cleric");
        assertThat(fallenCleric.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(fallenCleric));
        harness.passBothPriorities();

        assertThat(fallenCleric.isFaceDown()).isFalse();
    }
}
