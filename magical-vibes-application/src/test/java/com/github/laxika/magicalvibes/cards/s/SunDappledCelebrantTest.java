package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunDappledCelebrant.class, GrizzlyBears.class})
class SunDappledCelebrantTest extends BaseCardTest {

    @Test
    @DisplayName("Convoke taps creatures to help pay the generic cost")
    void castsWithConvoke() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SunDappledCelebrant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Sun-Dappled Celebrant")).isEqualTo(1);
    }

    @Test
    @DisplayName("White creatures can convoke both white mana symbols")
    void convokesColoredCost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SunDappledCelebrant());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SunDappledCelebrant());
        harness.setHand(player1, List.of(new SunDappledCelebrant()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Sun-Dappled Celebrant")).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Summoning-sick creatures can pay the entire cost with convoke")
    void convokesEntireCostWithSummoningSickCreatures() {
        List<Permanent> creatures = java.util.stream.IntStream.range(0, 6)
                .mapToObj(index -> harness.addToBattlefieldAndReturn(player1, new SunDappledCelebrant()))
                .toList();
        creatures.forEach(creature -> creature.setSummoningSick(true));
        harness.setHand(player1, List.of(new SunDappledCelebrant()));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                creatures.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        assertThat(creatures).allMatch(Permanent::isTapped);
        assertThat(countPermanents(player1, "Sun-Dappled Celebrant")).isEqualTo(7);
    }

    @Test
    @DisplayName("Convoke is optional when the full mana cost is paid")
    void castsWithoutConvoke() {
        harness.setHand(player1, List.of(new SunDappledCelebrant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sun-Dappled Celebrant");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A tapped creature cannot convoke")
    void rejectsTappedConvokeCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SunDappledCelebrant());
        creature.tap();
        harness.setHand(player1, List.of(new SunDappledCelebrant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(creature.getId()))).isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Sun-Dappled Celebrant");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's creature cannot convoke your spell")
    void rejectsOpponentsConvokeCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SunDappledCelebrant());
        harness.setHand(player1, List.of(new SunDappledCelebrant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(creature.getId()))).isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        harness.assertInHand(player1, "Sun-Dappled Celebrant");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature cannot be counted twice for convoke")
    void rejectsDuplicateConvokeCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SunDappledCelebrant());
        harness.setHand(player1, List.of(new SunDappledCelebrant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(creature.getId(), creature.getId()))).isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        harness.assertInHand(player1, "Sun-Dappled Celebrant");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Green creatures cannot convoke white mana symbols")
    void offColorConvokeCannotPayWhiteCost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SunDappledCelebrant()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(first.getId(), second.getId()))).isInstanceOf(IllegalStateException.class);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        harness.assertInHand(player1, "Sun-Dappled Celebrant");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Vigilance keeps Sun-Dappled Celebrant untapped when attacking")
    void vigilanceDoesNotTapWhenAttacking() {
        Permanent celebrant = addCreatureReady(player1, new SunDappledCelebrant());

        declareAttackers(List.of(0));

        assertThat(celebrant.isTapped()).isFalse();
    }
}
