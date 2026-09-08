package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZephyrFalcon;
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

@CardUsed({WinterBlast.class, AirElemental.class, GrizzlyBears.class, Forest.class, ZephyrFalcon.class})
class WinterBlastTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 taps both target creatures but only the flier takes 2 damage")
    void tapsAllTargetsDamagesOnlyFliers() {
        Permanent flier = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent grounded = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent untargetedFlier = harness.addToBattlefieldAndReturn(player2, new ZephyrFalcon());
        harness.setHand(player1, List.of(new WinterBlast()));
        harness.addMana(player1, ManaColor.GREEN, 3); // X=2: {2}{G} = 3

        harness.castSorcery(player1, 0, 2, List.of(flier.getId(), grounded.getId()));
        harness.passBothPriorities();

        assertThat(flier.isTapped()).isTrue();
        assertThat(grounded.isTapped()).isTrue();
        assertThat(flier.getMarkedDamage()).isEqualTo(2);
        assertThat(grounded.getMarkedDamage()).isEqualTo(0);
        assertThat(untargetedFlier.isTapped()).isFalse();
        assertThat(untargetedFlier.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("2 damage kills a targeted 1/1 flier")
    void killsLowToughnessFlier() {
        Permanent falcon = harness.addToBattlefieldAndReturn(player2, new ZephyrFalcon());
        harness.setHand(player1, List.of(new WinterBlast()));
        harness.addMana(player1, ManaColor.GREEN, 2); // X=1: {1}{G} = 2

        harness.castSorcery(player1, 0, 1, List.of(falcon.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Zephyr Falcon");
    }

    @Test
    @DisplayName("Must choose exactly X targets")
    void requiresExactlyXTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WinterBlast()));
        harness.addMana(player1, ManaColor.GREEN, 3); // X=2: {2}{G} = 3

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X=0 requires no targets and does nothing")
    void xZeroRequiresNoTargets() {
        Permanent flier = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new WinterBlast()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(flier.isTapped()).isFalse();
        assertThat(flier.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        UUID forestId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        harness.setHand(player1, List.of(new WinterBlast()));
        harness.addMana(player1, ManaColor.GREEN, 2); // X=1

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(forestId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
