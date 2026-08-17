package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntrusivePackbeastTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps two target creatures an opponent controls")
    void tapsTwoTargetCreatures() {
        Permanent bear1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent bear2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castPackbeast(List.of(bear1.getId(), bear2.getId()));

        assertThat(bear1.isTapped()).isTrue();
        assertThat(bear2.isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB can tap one target creature")
    void tapsOneTargetCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castPackbeast(List.of(bear.getId()));

        assertThat(bear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB can choose no targets")
    void canChooseNoTargets() {
        castPackbeast(List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof IntrusivePackbeast);
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new IntrusivePackbeast()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(ownBear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new IntrusivePackbeast()));
        addMana();

        UUID islandId = island.getId();
        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(islandId)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castPackbeast(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new IntrusivePackbeast()));
        addMana();

        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
