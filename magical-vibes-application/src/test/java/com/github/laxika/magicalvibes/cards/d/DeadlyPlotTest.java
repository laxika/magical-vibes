package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({DeadlyPlot.class, Forest.class, Gravecrawler.class, GrizzlyBears.class})
class DeadlyPlotTest extends BaseCardTest {

    @Test
    @DisplayName("The destroy mode destroys a target creature")
    void destroysTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(0, List.of(creature.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The reanimation mode returns a Zombie tapped")
    void returnsTargetZombieTapped() {
        Card zombie = new Gravecrawler();
        Card nonZombie = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(zombie, nonZombie));
        harness.setHand(player1, List.of(new DeadlyPlot()));
        addMana();

        harness.castModalInstantWithModes(player1, 0, 1, new int[]{1}, zombie.getId(), List.of());
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(zombie.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
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
