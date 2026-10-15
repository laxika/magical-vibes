package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AlmsCollector;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JacesArchivist.class, RuneclawBear.class, Plains.class})
class JacesArchivistTest extends BaseCardTest {

    @Test
    @DisplayName("Every player draws equal to the largest hand discarded")
    void everyoneDrawsGreatestDiscarded() {
        addReadyArchivist();
        harness.setHand(player1, List.of(new RuneclawBear()));
        harness.setHand(player2, List.of(new Plains(), new Plains(), new Plains()));

        activate();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Discarded cards are not redrawn - draws come off the library")
    void discardedCardsGoToGraveyardNotBackToHand() {
        addReadyArchivist();
        harness.setHand(player1, List.of(new RuneclawBear(), new RuneclawBear()));
        harness.setHand(player2, List.of());

        activate();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> gd.playerGraveyards.get(player1.getId()).contains(card));
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Nobody draws when every hand is empty")
    void noDrawsWhenAllHandsEmpty() {
        addReadyArchivist();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        activate();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Activation pays blue mana and taps without discarding until resolution")
    void paysCostsBeforeResolution() {
        Permanent archivist = addReadyArchivist();
        RuneclawBear original = new RuneclawBear();
        harness.setHand(player1, List.of(original));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(archivist.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(original);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(original);
    }

    @Test
    @DisplayName("Cannot activate without blue mana")
    void cannotActivateWithoutBlueMana() {
        Permanent archivist = addReadyArchivist();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(archivist.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent archivist = addReadyArchivist();
        archivist.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWhileSummoningSick() {
        Permanent archivist = harness.addToBattlefieldAndReturn(player1, new JacesArchivist());
        archivist.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hand sizes are measured at resolution and the ability survives its source")
    void usesResolutionHandsAfterSourceLeaves() {
        addReadyArchivist();
        harness.setHand(player1, List.of(new RuneclawBear()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player2, List.of(new Plains(), new Plains(), new Plains()));
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @CardUsed({AlmsCollector.class})
    @DisplayName("Alms Collector replaces the opponent's entire multi-card draw")
    void collectorReplacesMultiCardDraw() {
        addReadyArchivist();
        harness.addToBattlefield(player1, new AlmsCollector());
        harness.setHand(player1, List.of(new Plains(), new Plains(), new Plains()));
        harness.setHand(player2, List.of(new RuneclawBear()));
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Plains(), new Plains(), new Plains()));
        harness.setLibrary(player2, List.of(new Plains(), new Plains(), new Plains(), new Plains(), new Plains()));

        activate();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    private Permanent addReadyArchivist() {
        return addCreatureReady(player1, new JacesArchivist());
    }

    private void activate() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
