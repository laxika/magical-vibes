package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Ionize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PistonFistCyclops.class, Divination.class, GrizzlyBears.class, Shock.class, Ionize.class})
class PistonFistCyclopsTest extends BaseCardTest {

    @BeforeEach
    void setUpTest() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Can attack after its controller casts an instant")
    void canAttackAfterInstant() {
        addCreatureReady(player1, new PistonFistCyclops());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        declareAttackers(List.of(0));

        Permanent cyclops = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(cyclops.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Can attack after its controller casts a sorcery")
    void canAttackAfterSorcery() {
        addCreatureReady(player1, new PistonFistCyclops());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        declareAttackers(List.of(0));

        Permanent cyclops = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(cyclops.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Cannot attack when only a creature spell was cast")
    void cannotAttackAfterCreatureSpell() {
        addCreatureReady(player1, new PistonFistCyclops());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attack permission wears off at the end of the turn")
    void attackPermissionWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new PistonFistCyclops());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotAttackWithoutCastingInstantOrSorcery() {
        addCreatureReady(player1, new PistonFistCyclops());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsInstantDoesNotGrantAttackPermission() {
        addCreatureReady(player1, new PistonFistCyclops());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countsInstantCastBeforeCyclopsEnteredBattlefield() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        Permanent cyclops = addCreatureReady(player1, new PistonFistCyclops());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        assertThat(cyclops.isAttacking()).isTrue();
    }

    @Test
    void attackPermissionDoesNotBypassSummoningSickness() {
        harness.addToBattlefield(player1, new PistonFistCyclops());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void counteredInstantStillGrantsAttackPermission() {
        Permanent cyclops = addCreatureReady(player1, new PistonFistCyclops());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Ionize()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, shock.getId());
        harness.assertInGraveyard(player1, "Shock");
        harness.assertLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        assertThat(cyclops.isAttacking()).isTrue();
    }
}
