package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BirdMaiden;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeeperOfTheNineGales.class, BirdMaiden.class, Forest.class, GrizzlyBears.class})
class KeeperOfTheNineGalesTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target permanent and taps the source and two Birds")
    void returnsTargetPermanent() {
        Permanent keeper = addCreatureReady(player1, new KeeperOfTheNineGales());
        Permanent bird1 = addCreatureReady(player1, new BirdMaiden());
        Permanent bird2 = addCreatureReady(player1, new BirdMaiden());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(player1, keeper), 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(keeper.isTapped()).isTrue();
        assertThat(bird1.isTapped()).isTrue();
        assertThat(bird2.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can return a noncreature permanent")
    void returnsNoncreaturePermanent() {
        Permanent keeper = addCreatureReady(player1, new KeeperOfTheNineGales());
        addCreatureReady(player1, new BirdMaiden());
        addCreatureReady(player1, new BirdMaiden());
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
        addCreatureReady(player1, new BirdMaiden());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, keeper), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not count non-Birds toward the tap cost")
    void requiresBirdsForTapCost() {
        Permanent keeper = addCreatureReady(player1, new KeeperOfTheNineGales());
        addCreatureReady(player1, new BirdMaiden());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, keeper), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
