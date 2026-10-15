package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeylineOfHope;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FearOfExposure.class, Forest.class, GrizzlyBears.class, LeylineOfHope.class})
class FearOfExposureTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a creature and a land as an additional cost")
    void tapsCreatureAndLand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new FearOfExposure()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreatureTappingPermanents(player1, 0, List.of(creature.getId(), land.getId()));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(land.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Fear of Exposure");
    }

    @Test
    @DisplayName("Requires exactly two creatures and/or lands")
    void rejectsFewerThanTwoEligiblePermanents() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FearOfExposure()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        harness.assertInHand(player1, "Fear of Exposure");
    }

    @Test
    void tapsTwoLandsBeforeTheSpellResolves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new FearOfExposure()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreatureTappingPermanents(player1, 0, List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Fear of Exposure");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Fear of Exposure");
    }

    @Test
    void canTapTwoSummoningSickCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new FearOfExposure());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new FearOfExposure());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        harness.setHand(player1, List.of(new FearOfExposure()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreatureTappingPermanents(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Fear of Exposure")).isEqualTo(3);
    }

    @Test
    void cannotTapTheSamePermanentTwice() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new FearOfExposure()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(player1, 0,
                List.of(land.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(land.isTapped()).isFalse();
        harness.assertInHand(player1, "Fear of Exposure");
    }

    @Test
    void cannotTapAnAlreadyTappedPermanent() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());
        second.tap();
        harness.setHand(player1, List.of(new FearOfExposure()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(player1, 0,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isTrue();
        harness.assertInHand(player1, "Fear of Exposure");
    }

    @Test
    void cannotTapAnOpponentsPermanent() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new FearOfExposure()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(player1, 0,
                List.of(own.getId(), opposing.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(own.isTapped()).isFalse();
        assertThat(opposing.isTapped()).isFalse();
        harness.assertInHand(player1, "Fear of Exposure");
    }

    @Test
    void cannotTapAPermanentThatIsNeitherACreatureNorALand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new LeylineOfHope());
        harness.setHand(player1, List.of(new FearOfExposure()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(player1, 0,
                List.of(land.getId(), enchantment.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(land.isTapped()).isFalse();
        assertThat(enchantment.isTapped()).isFalse();
        harness.assertInHand(player1, "Fear of Exposure");
    }

    @Test
    void cannotPayWithThreePermanents() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new FearOfExposure()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(third.isTapped()).isFalse();
        harness.assertInHand(player1, "Fear of Exposure");
    }

    @Test
    void cannotOmitTheAdditionalCost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new FearOfExposure()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        harness.assertInHand(player1, "Fear of Exposure");
    }

    @Test
    void tramplesOverLethalDamageAssignedToBlocker() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        addCreatureReady(player1, new FearOfExposure());
        Permanent blocker = addCreatureReady(player2, new FearOfExposure());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 4, player2.getId(), 1));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Fear of Exposure");
        harness.assertInGraveyard(player2, "Fear of Exposure");
    }
}
