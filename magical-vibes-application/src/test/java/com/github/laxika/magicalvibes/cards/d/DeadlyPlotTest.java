package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HungryGhoul;
import com.github.laxika.magicalvibes.cards.l.LilianaDreadhordeGeneral;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadlyPlot.class, Forest.class, HungryGhoul.class, BearCub.class, LilianaDreadhordeGeneral.class})
class DeadlyPlotTest extends BaseCardTest {

    @Test
    @DisplayName("The destroy mode destroys a target creature")
    void destroysTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BearCub());
        cast(0, List.of(creature.getId()));

        harness.assertNotOnBattlefield(player2, "Bear Cub");
    }

    @Test
    @DisplayName("The reanimation mode returns a Zombie tapped")
    void returnsTargetZombieTapped() {
        Card zombie = new HungryGhoul();
        Card nonZombie = new BearCub();
        harness.setGraveyard(player1, List.of(zombie, nonZombie));
        harness.setHand(player1, List.of(new DeadlyPlot()));
        addMana();

        harness.castModalInstantWithModes(player1, 0, 1, new int[]{1}, zombie.getId(), List.of());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Hungry Ghoul");
        assertThat(returned.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Bear Cub");
    }

    @Test
    @DisplayName("The destroy mode rejects a land target")
    void destroyModeRejectsLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new DeadlyPlot()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The destroy mode destroys a target planeswalker")
    void destroysTargetPlaneswalker() {
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new LilianaDreadhordeGeneral());

        cast(0, List.of(planeswalker.getId()));

        harness.assertNotOnBattlefield(player2, "Liliana, Dreadhorde General");
        harness.assertInGraveyard(player2, "Liliana, Dreadhorde General");
    }

    @Test
    @DisplayName("The reanimation mode rejects a non-Zombie creature")
    void reanimationRejectsNonZombie() {
        Card creature = new BearCub();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DeadlyPlot()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, new int[]{1}, creature.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reanimation mode rejects a Zombie in the opponent's graveyard")
    void reanimationRejectsOpponentsZombie() {
        Card zombie = new HungryGhoul();
        harness.setGraveyard(player2, List.of(zombie));
        harness.setHand(player1, List.of(new DeadlyPlot()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, new int[]{1}, zombie.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reanimation mode rejects a noncreature card")
    void reanimationRejectsNoncreature() {
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.setHand(player1, List.of(new DeadlyPlot()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, new int[]{1}, land.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reanimation mode does not substitute another Zombie when its target leaves")
    void reanimationFizzlesWhenTargetLeavesGraveyard() {
        Card target = new HungryGhoul();
        Card otherZombie = new HungryGhoul();
        harness.setGraveyard(player1, List.of(target, otherZombie));
        harness.setHand(player1, List.of(new DeadlyPlot()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, new int[]{1}, target.getId(), List.of());

        harness.setGraveyard(player1, List.of(otherZombie));
        harness.setHand(player2, List.of(target));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hungry Ghoul");
        harness.assertNotOnBattlefield(player2, "Hungry Ghoul");
        harness.assertInGraveyard(player1, "Hungry Ghoul");
        harness.assertInGraveyard(player1, "Deadly Plot");
        harness.assertInHand(player2, "Hungry Ghoul");
    }

    private void cast(int mode, List<java.util.UUID> targets) {
        harness.setHand(player1, List.of(new DeadlyPlot()));
        addMana();
        harness.castModalInstant(player1, 0, mode, targets);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
