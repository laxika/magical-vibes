package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnragedGiant.class, Ornithopter.class})
class EnragedGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Enraged Giant can attack immediately due to haste")
    void canAttackImmediatelyDueToHaste() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new EnragedGiant());
        giant.setSummoningSick(true);

        assertThatCode(() -> declareAttackers(List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    void improvisePaysGenericManaWithSummoningSickArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.setSummoningSick(true);
        harness.setHand(player1, List.of(new EnragedGiant()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId()));

        assertThat(artifact.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Enraged Giant");
        harness.assertNotInHand(player1, "Enraged Giant");
    }

    @Test
    void improviseCannotPayRedMana() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new EnragedGiant()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> gs.playCard(
                gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(artifact.isTapped()).isFalse();
        harness.assertInHand(player1, "Enraged Giant");
    }

    @Test
    void improviseCannotTapTappedArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.tap();
        harness.setHand(player1, List.of(new EnragedGiant()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> gs.playCard(
                gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        harness.assertInHand(player1, "Enraged Giant");
    }

    @Test
    void improviseCannotTapNonartifactCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EnragedGiant());
        harness.setHand(player1, List.of(new EnragedGiant()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> gs.playCard(
                gd, player1, 0, 0, null, null, List.of(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("is not an artifact");
        assertThat(creature.isTapped()).isFalse();
        harness.assertInHand(player1, "Enraged Giant");
    }

    @Test
    void trampleDealsExcessDamageAfterLethalDamageToBlocker() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EnragedGiant());
        Permanent blocker = addCreatureReady(player2, new Ornithopter());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertOnBattlefield(player1, "Enraged Giant");
    }
}
