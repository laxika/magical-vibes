package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JalumTome;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Aetherjacket.class, JalumTome.class, GrizzlyBears.class})
class AetherjacketTest extends BaseCardTest {

    @Test
    @DisplayName("Pays the activation cost, sacrifices itself, and destroys another artifact")
    void sacrificesItselfAndDestroysAnotherArtifact() {
        addCreatureReady(player1, new Aetherjacket());
        harness.addToBattlefield(player2, new JalumTome());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Jalum Tome"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aetherjacket");
        harness.assertInGraveyard(player2, "Jalum Tome");
    }

    @Test
    @DisplayName("Cannot target itself or a non-artifact")
    void cannotTargetItselfOrNonArtifact() {
        Permanent source = addCreatureReady(player1, new Aetherjacket());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate only as a sorcery")
    void canActivateOnlyAsSorcery() {
        addCreatureReady(player1, new Aetherjacket());
        harness.addToBattlefield(player2, new JalumTome());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, harness.getPermanentId(player2, "Jalum Tome")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void flyingPreventsBlockingByAGroundCreatureAndVigilanceKeepsItUntapped() {
        Permanent source = addCreatureReady(player1, new Aetherjacket());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(source.isAttacking()).isTrue();
        assertThat(source.isTapped()).isFalse();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void canDestroyAnotherAetherjacketItControlsDuringPostcombatMain() {
        addCreatureReady(player1, new Aetherjacket());
        Permanent target = addCreatureReady(player1, new Aetherjacket());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(countPermanents(player1, "Aetherjacket")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Aetherjacket");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Aetherjacket");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void cannotActivateDuringCombat() {
        addCreatureReady(player1, new Aetherjacket());
        Permanent target = addCreatureReady(player2, new Aetherjacket());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Aetherjacket");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileAnotherAbilityIsOnTheStack() {
        addCreatureReady(player1, new Aetherjacket());
        addCreatureReady(player1, new Aetherjacket());
        Permanent target = addCreatureReady(player2, new Aetherjacket());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Aetherjacket")).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    void cannotActivateWithInsufficientMana() {
        addCreatureReady(player1, new Aetherjacket());
        Permanent target = addCreatureReady(player2, new Aetherjacket());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Aetherjacket");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent source = addCreatureReady(player1, new Aetherjacket());
        source.tap();
        Permanent target = addCreatureReady(player2, new Aetherjacket());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Aetherjacket");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent source = addCreatureReady(player1, new Aetherjacket());
        source.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new Aetherjacket());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Aetherjacket");
        assertThat(gd.stack).isEmpty();
    }
}
