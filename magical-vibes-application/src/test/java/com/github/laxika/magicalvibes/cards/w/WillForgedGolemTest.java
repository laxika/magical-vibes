package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WillForgedGolem.class, RuneclawBear.class})
class WillForgedGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Convoke taps creatures to help pay the generic cost")
    void castsWithConvoke() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new WillForgedGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Will-Forged Golem");
    }

    @Test
    @DisplayName("Six summoning-sick colorless creatures can pay the entire generic cost")
    void castsEntirelyWithSummoningSickColorlessCreatures() {
        List<Permanent> creatures = IntStream.range(0, 6)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new WillForgedGolem()))
                .toList();
        creatures.forEach(creature -> creature.setSummoningSick(true));
        harness.setHand(player1, List.of(new WillForgedGolem()));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                creatures.stream().map(Permanent::getId).toList());

        assertThat(creatures).allMatch(Permanent::isTapped);
        harness.assertNotInHand(player1, "Will-Forged Golem");
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof WillForgedGolem)
                .hasSize(7);
    }

    @Test
    @DisplayName("Convoke is optional and paying six mana leaves creatures untapped")
    void castsWithoutConvoke() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new WillForgedGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Will-Forged Golem");
    }

    @Test
    @DisplayName("An already tapped creature cannot convoke")
    void cannotConvokeWithTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        creature.tap();
        harness.setHand(player1, List.of(new WillForgedGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Will-Forged Golem");
        harness.assertNotOnBattlefield(player1, "Will-Forged Golem");
    }

    @Test
    @DisplayName("An opponent's creature cannot convoke")
    void cannotConvokeWithOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new WillForgedGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        harness.assertInHand(player1, "Will-Forged Golem");
        harness.assertNotOnBattlefield(player1, "Will-Forged Golem");
    }

    @Test
    @DisplayName("A creature cannot pay for two mana by being selected twice")
    void cannotConvokeWithDuplicateCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new WillForgedGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        harness.assertInHand(player1, "Will-Forged Golem");
        harness.assertNotOnBattlefield(player1, "Will-Forged Golem");
    }

    @Test
    @DisplayName("One convoking creature and four mana do not pay the six-mana cost")
    void cannotCastWithInsufficientPayment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new WillForgedGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Will-Forged Golem");
        harness.assertNotOnBattlefield(player1, "Will-Forged Golem");
    }
    @Test
    @DisplayName("Every selected convoking creature replaces one mana even when enough mana is available")
    void convokePreservesManaWhenPoolCouldPayEntireCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new WillForgedGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Will-Forged Golem");
    }
}
