package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeeperOfTheMind.class, RagingGoblin.class})
class KeeperOfTheMindTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when activated targeting an opponent with at least two more cards")
    void drawsCard() {
        Permanent keeper = readyKeeper(1, 3);
        harness.setLibrary(player1, List.of(new RagingGoblin()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(keeper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The hand-size condition is checked only when activating")
    void handSizeConditionIsCheckedOnlyOnActivation() {
        readyKeeper(1, 3);
        harness.setLibrary(player1, List.of(new RagingGoblin()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.setHand(player2, List.of(new RagingGoblin()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot activate when the opponent has only one more card")
    void cannotActivateWithoutTwoMoreCards() {
        readyKeeper(1, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        readyKeeper(1, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without choosing an opponent")
    void cannotActivateWithoutTarget() {
        Permanent keeper = readyKeeper(1, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(keeper.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent keeper = readyKeeper(1, 3);
        keeper.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent keeper = readyKeeper(1, 3);
        keeper.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot pay the blue activation cost with red mana")
    void cannotPayWithRedMana() {
        Permanent keeper = readyKeeper(1, 3);
        gd.playerManaPools.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(keeper.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Still draws if the controller's hand grows before resolution")
    void drawsAfterControllerHandGrows() {
        readyKeeper(1, 3);
        harness.setLibrary(player1, List.of(new RagingGoblin()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.setHand(player1, cards(4));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The activated ability resolves after its source leaves the battlefield")
    void drawsAfterSourceLeavesBattlefield() {
        readyKeeper(0, 2);
        harness.setLibrary(player1, List.of(new RagingGoblin()));

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    private Permanent readyKeeper(int controllerHandSize, int opponentHandSize) {
        harness.setHand(player1, cards(controllerHandSize));
        harness.setHand(player2, cards(opponentHandSize));
        Permanent keeper = addCreatureReady(player1, new KeeperOfTheMind());
        harness.addMana(player1, ManaColor.BLUE, 1);
        return keeper;
    }

    private List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new RagingGoblin());
        }
        return cards;
    }
}
