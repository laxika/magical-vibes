package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorderlandMarauder;
import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.c.ChandraPyromaster;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StokeTheFlames.class, RuneclawBear.class, BorderlandMarauder.class,
        BronzeSable.class, ChandraPyromaster.class})
class StokeTheFlamesTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target player")
    void dealsDamageToPlayer() {
        harness.setHand(player1, List.of(new StokeTheFlames()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Deals 4 damage to target creature, destroying it")
    void dealsDamageToCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new StokeTheFlames()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Convoke taps creatures and reduces the mana needed to cast the spell")
    void castsWithConvoke() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new StokeTheFlames()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithConvoke(player1, 0, List.of(player2.getId()),
                List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Can target its own controller")
    void damagesItsController() {
        harness.setHand(player1, List.of(new StokeTheFlames()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deals damage directly to a planeswalker")
    void damagesPlaneswalker() {
        Permanent chandra = harness.enterBattlefieldAndReturn(player2, new ChandraPyromaster());
        harness.setHand(player1, List.of(new StokeTheFlames()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, chandra.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Chandra, Pyromaster");
        harness.assertInGraveyard(player2, "Chandra, Pyromaster");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Red and green creatures with summoning sickness can pay the entire cost")
    void convokesWithoutMana() {
        Permanent firstRed = harness.addToBattlefieldAndReturn(player1, new BorderlandMarauder());
        Permanent secondRed = harness.addToBattlefieldAndReturn(player1, new BorderlandMarauder());
        Permanent firstGreen = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent secondGreen = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new StokeTheFlames()));

        harness.castInstantWithConvoke(player1, 0, List.of(player2.getId()),
                List.of(firstGreen.getId(), firstRed.getId(), secondGreen.getId(), secondRed.getId()));

        assertThat(List.of(firstRed, secondRed, firstGreen, secondGreen)).allMatch(Permanent::isTapped);
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Colorless creatures can convoke the generic portion of the cost")
    void convokesWithColorlessCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        harness.setHand(player1, List.of(new StokeTheFlames()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithConvoke(player1, 0, List.of(player2.getId()),
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Green creatures cannot convoke a missing red mana")
    void cannotConvokeRedWithGreenCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new StokeTheFlames()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(player2.getId()),
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(List.of(first, second, third)).noneMatch(Permanent::isTapped);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Stoke the Flames");
    }

    @Test
    @DisplayName("Cannot convoke with an already tapped creature")
    void cannotConvokeTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        creature.tap();
        harness.setHand(player1, List.of(new StokeTheFlames()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(player2.getId()),
                List.of(creature.getId()))).isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Stoke the Flames");
    }

    @Test
    @DisplayName("Cannot convoke with an opponent's creature")
    void cannotConvokeOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new StokeTheFlames()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(player2.getId()),
                List.of(creature.getId()))).isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Stoke the Flames");
    }

    @Test
    @DisplayName("Cannot use one creature twice for convoke")
    void cannotConvokeSameCreatureTwice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new StokeTheFlames()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(player2.getId()),
                List.of(creature.getId(), creature.getId()))).isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Stoke the Flames");
    }
}
