package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChandrasRevolution.class, ColossalDreadmaw.class, GrizzlyBears.class,
        Mountain.class, Forest.class})
class ChandrasRevolutionTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to a creature and taps a land that skips its next untap")
    void damagesCreatureAndLocksLand() {
        Permanent creature = addCreatureReady(player2, new ColossalDreadmaw());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());

        castRevolution(creature.getId(), land.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(4);
        assertThat(land.isTapped()).isTrue();
        assertThat(land.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Requires a creature target followed by a land target")
    void rejectsWrongTargetTypes() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new ChandrasRevolution()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void landSkipsOnlyItsControllersNextUntapStep() {
        Permanent creature = addCreatureReady(player2, new ColossalDreadmaw());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());

        castRevolution(creature.getId(), land.getId());

        harness.performUntapStep(player1);
        assertThat(land.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(land.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    void alreadyTappedLandStillSkipsItsNextUntap() {
        Permanent creature = addCreatureReady(player2, new ColossalDreadmaw());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        land.tap();

        castRevolution(creature.getId(), land.getId());

        harness.performUntapStep(player2);
        assertThat(land.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    void lethalDamageDoesNotPreventLandLock() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        castRevolution(creature.getId(), land.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(land.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    void stillLocksLandWhenCreatureTargetLeavesBattlefield() {
        Permanent creature = addCreatureReady(player2, new ColossalDreadmaw());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new ChandrasRevolution()));
        addMana();
        harness.castSorcery(player1, 0, List.of(creature.getId(), land.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.setGraveyard(player2, List.of(creature.getCard()));

        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(land.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Chandra's Revolution");
    }

    @Test
    void stillDamagesCreatureWhenLandTargetLeavesBattlefield() {
        Permanent creature = addCreatureReady(player2, new ColossalDreadmaw());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new ChandrasRevolution()));
        addMana();
        harness.castSorcery(player1, 0, List.of(creature.getId(), land.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(land);
        harness.setGraveyard(player2, List.of(land.getCard()));

        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(4);
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getSkipUntapCount()).isZero();
        harness.assertInGraveyard(player1, "Chandra's Revolution");
    }

    @Test
    void canTargetOwnCreatureAndOwnLand() {
        Permanent creature = addCreatureReady(player1, new ColossalDreadmaw());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());

        castRevolution(creature.getId(), land.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(4);
        assertThat(land.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(land.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(land.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(land.isTapped()).isFalse();
    }

    private void castRevolution(UUID creatureId, UUID landId) {
        harness.setHand(player1, List.of(new ChandrasRevolution()));
        addMana();
        harness.castSorcery(player1, 0, List.of(creatureId, landId));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
