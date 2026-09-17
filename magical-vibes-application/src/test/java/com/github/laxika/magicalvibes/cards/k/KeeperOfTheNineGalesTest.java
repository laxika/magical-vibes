package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AvenEnvoy;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeeperOfTheNineGales.class, AvenEnvoy.class, Forest.class})
class KeeperOfTheNineGalesTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target permanent and taps the source and two Birds")
    void returnsTargetPermanent() {
        Permanent keeper = addCreatureReady(player1, new KeeperOfTheNineGales());
        Permanent bird1 = addCreatureReady(player1, new AvenEnvoy());
        Permanent bird2 = addCreatureReady(player1, new AvenEnvoy());
        Permanent target = addCreatureReady(player2, new AvenEnvoy());

        harness.activateAbility(player1, battlefieldIndex(player1, keeper), 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInHand(player2, "Aven Envoy");
        assertThat(keeper.isTapped()).isTrue();
        assertThat(bird1.isTapped()).isTrue();
        assertThat(bird2.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can return a noncreature permanent")
    void returnsNoncreaturePermanent() {
        Permanent keeper = addCreatureReady(player1, new KeeperOfTheNineGales());
        addCreatureReady(player1, new AvenEnvoy());
        addCreatureReady(player1, new AvenEnvoy());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, battlefieldIndex(player1, keeper), 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(forest);
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Requires two other untapped Birds")
    void requiresTwoOtherUntappedBirds() {
        Permanent keeper = addCreatureReady(player1, new KeeperOfTheNineGales());
        addCreatureReady(player1, new AvenEnvoy());
        Permanent target = addCreatureReady(player2, new AvenEnvoy());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, keeper), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not count non-Birds toward the tap cost")
    void requiresBirdsForTapCost() {
        Permanent keeper = addCreatureReady(player1, new KeeperOfTheNineGales());
        addCreatureReady(player1, new AvenEnvoy());
        addCreatureReady(player1, new Forest());
        Permanent target = addCreatureReady(player2, new AvenEnvoy());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, keeper), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not count a tapped Bird toward the tap cost")
    void doesNotCountTappedBird() {
        Permanent keeper = addCreatureReady(player1, new KeeperOfTheNineGales());
        Permanent tappedBird = addCreatureReady(player1, new AvenEnvoy());
        Permanent untappedBird = addCreatureReady(player1, new AvenEnvoy());
        tappedBird.tap();
        Permanent target = addCreatureReady(player2, new AvenEnvoy());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, keeper), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(keeper.isTapped()).isFalse();
        assertThat(tappedBird.isTapped()).isTrue();
        assertThat(untappedBird.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate while the Keeper is tapped")
    void requiresUntappedKeeper() {
        Permanent keeper = addCreatureReady(player1, new KeeperOfTheNineGales());
        Permanent bird1 = addCreatureReady(player1, new AvenEnvoy());
        Permanent bird2 = addCreatureReady(player1, new AvenEnvoy());
        Permanent target = addCreatureReady(player2, new AvenEnvoy());
        keeper.tap();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, keeper), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(keeper.isTapped()).isTrue();
        assertThat(bird1.isTapped()).isFalse();
        assertThat(bird2.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not count Birds controlled by an opponent")
    void doesNotCountOpponentBirds() {
        Permanent keeper = addCreatureReady(player1, new KeeperOfTheNineGales());
        Permanent opponentBird1 = addCreatureReady(player2, new AvenEnvoy());
        Permanent opponentBird2 = addCreatureReady(player2, new AvenEnvoy());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, keeper), 0, null, opponentBird1.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(keeper.isTapped()).isFalse();
        assertThat(opponentBird1.isTapped()).isFalse();
        assertThat(opponentBird2.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Taps exactly the two Birds chosen when more are available")
    void tapsExactlyTwoChosenBirds() {
        Permanent keeper = addCreatureReady(player1, new KeeperOfTheNineGales());
        Permanent firstBird = addCreatureReady(player1, new AvenEnvoy());
        Permanent secondBird = addCreatureReady(player1, new AvenEnvoy());
        Permanent thirdBird = addCreatureReady(player1, new AvenEnvoy());
        Permanent target = addCreatureReady(player2, new AvenEnvoy());

        harness.activateAbility(player1, battlefieldIndex(player1, keeper), 0, null, target.getId());
        harness.handlePermanentChosen(player1, firstBird.getId());
        harness.handlePermanentChosen(player1, secondBird.getId());
        harness.passBothPriorities();

        assertThat(keeper.isTapped()).isTrue();
        assertThat(firstBird.isTapped()).isTrue();
        assertThat(secondBird.isTapped()).isTrue();
        assertThat(thirdBird.isTapped()).isFalse();
        harness.assertInHand(player2, "Aven Envoy");
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
