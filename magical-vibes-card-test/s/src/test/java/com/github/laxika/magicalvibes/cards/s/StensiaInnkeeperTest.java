package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({StensiaInnkeeper.class, Forest.class})
class StensiaInnkeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target land an opponent controls and keeps it tapped through its next untap step")
    void tapsAndLocksOpponentsLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");
        harness.setHand(player1, List.of(new StensiaInnkeeper()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, forestId);
        resolveAllTriggers();

        Permanent forest = findPermanent(player2, "Forest");
        assertThat(forest.isTapped()).isTrue();
        assertThat(forest.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a land you control")
    void cannotTargetOwnLand() {
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");
        harness.setHand(player1, List.of(new StensiaInnkeeper()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, forestId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void landSkipsOnlyItsControllersNextUntapEvenAfterInnkeeperLeaves() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new StensiaInnkeeper()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0, forest.getId());
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Stensia Innkeeper"));

        harness.performUntapStep(player1);
        assertThat(forest.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(forest.isTapped()).isTrue();
        harness.performUntapStep(player1);
        harness.performUntapStep(player2);
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    void alreadyTappedLandStillSkipsNextUntap() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();
        harness.setHand(player1, List.of(new StensiaInnkeeper()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0, forest.getId());
        resolveAllTriggers();
        harness.performUntapStep(player2);
        assertThat(forest.isTapped()).isTrue();
        harness.performUntapStep(player1);
        harness.performUntapStep(player2);
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @CardUsed({StensiaInnkeeper.class})
    void cannotTargetOpponentsNonlandCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new StensiaInnkeeper());
        harness.setHand(player1, List.of(new StensiaInnkeeper()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({StensiaInnkeeper.class})
    void entersWithoutAnyLegalLandTarget() {
        harness.setHand(player1, List.of(new StensiaInnkeeper()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Stensia Innkeeper");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
