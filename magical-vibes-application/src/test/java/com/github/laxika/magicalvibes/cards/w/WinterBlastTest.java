package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.z.ZephyrFalcon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WinterBlast.class, AirElemental.class, GrizzlyBears.class, Forest.class, ZephyrFalcon.class,
        Unsummon.class})
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

        harness.castAndResolveSorcery(player1, 0, 1, falcon.getId());

        harness.assertInGraveyard(player2, "Zephyr Falcon");
    }

    @Test
    @DisplayName("Can target a creature I control")
    void canTargetOwnCreature() {
        Permanent ownFlier = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new WinterBlast()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 1, ownFlier.getId());

        assertThat(ownFlier.isTapped()).isTrue();
        assertThat(ownFlier.getMarkedDamage()).isEqualTo(2);
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

        harness.castAndResolveSorcery(player1, 0, 0);

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

    @Test
    @DisplayName("An already tapped flier still takes damage")
    void damagesAlreadyTappedFlier() {
        Permanent flier = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        flier.tap();
        harness.setHand(player1, List.of(new WinterBlast()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 1, flier.getId());

        assertThat(flier.isTapped()).isTrue();
        assertThat(flier.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The same creature cannot be chosen twice")
    void rejectsDuplicateTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new WinterBlast()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2,
                List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A remaining legal target is tapped and damaged when another target leaves")
    void resolvesForRemainingTarget() {
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent returned = harness.addToBattlefieldAndReturn(player2, new ZephyrFalcon());
        harness.setHand(player1, List.of(new WinterBlast()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 2, List.of(remaining.getId(), returned.getId()));
        harness.castAndResolveInstant(player2, 0, returned.getId());
        harness.passBothPriorities();

        assertThat(remaining.isTapped()).isTrue();
        assertThat(remaining.getMarkedDamage()).isEqualTo(2);
        harness.assertInHand(player2, "Zephyr Falcon");
        harness.assertNotOnBattlefield(player2, "Zephyr Falcon");
        harness.assertInGraveyard(player1, "Winter Blast");
    }

    @Test
    @DisplayName("X can exceed 100 when enough distinct creatures are available")
    void canTargetMoreThanOneHundredCreatures() {
        List<Permanent> targets = IntStream.range(0, 101)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()))
                .toList();
        harness.setHand(player1, List.of(new WinterBlast()));
        harness.addMana(player1, ManaColor.GREEN, 102);

        harness.castSorcery(player1, 0, 101, targets.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        assertThat(targets).allSatisfy(target -> {
            assertThat(target.isTapped()).isTrue();
            assertThat(target.getMarkedDamage()).isZero();
        });
        harness.assertInGraveyard(player1, "Winter Blast");
    }
}
