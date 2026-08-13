package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KiorasFollowerTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps a tapped target permanent")
    void untapsTappedTargetPermanent() {
        addReadyFollower(player1);
        harness.addToBattlefield(player2, new Forest());
        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activating the ability taps Kiora's Follower as a cost")
    void activatingAbilityTapsSource() {
        Permanent follower = addReadyFollower(player1);
        harness.addToBattlefield(player2, new Forest());
        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(follower.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target Kiora's Follower itself")
    void cannotTargetItself() {
        Permanent follower = addReadyFollower(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, follower.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another permanent");
    }

    private Permanent addReadyFollower(Player player) {
        Permanent follower = new Permanent(new KiorasFollower());
        follower.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(follower);
        return follower;
    }
}
