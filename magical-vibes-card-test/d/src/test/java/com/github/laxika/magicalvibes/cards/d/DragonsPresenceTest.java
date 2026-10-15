package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AncestorDragon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonsPresence.class, GrizzlyBears.class, AncestorDragon.class})
class DragonsPresenceTest extends BaseCardTest {

    @Test
    void dealsFiveDamageToAnAttackingCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setAttacking(true);

        cast(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void dealsFiveDamageToABlockingCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setBlocking(true);

        cast(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetAnIdleCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new DragonsPresence()));
        addMana();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    @Test
    void marksExactlyFiveDamageOnASurvivingCreature() {
        Permanent target = addCreatureReady(player2, new AncestorDragon());
        target.setBlocking(true);

        cast(target);

        harness.assertOnBattlefield(player2, "Ancestor Dragon");
        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    void canDamageItsControllersOwnAttackingCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.setAttacking(true);

        cast(target);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void doesNotDamageACreatureThatLeavesCombatBeforeResolution(boolean attacking) {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setAttacking(attacking);
        target.setBlocking(!attacking);
        harness.setHand(player1, List.of(new DragonsPresence()));
        addMana();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.castInstant(player1, 0, target.getId());

        target.setAttacking(false);
        target.setBlocking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Dragon's Presence");
        assertThat(gd.stack).isEmpty();
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new DragonsPresence()));
        addMana();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
