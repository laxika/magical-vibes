package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KiorasFollower.class, Forest.class})
class KiorasFollowerTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps a tapped target permanent")
    void untapsTappedTargetPermanent() {
        addCreatureReady(player1, new KiorasFollower());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activating the ability taps Kiora's Follower as a cost")
    void activatingAbilityTapsSource() {
        Permanent follower = addCreatureReady(player1, new KiorasFollower());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(follower.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target Kiora's Follower itself")
    void cannotTargetItself() {
        Permanent follower = addCreatureReady(player1, new KiorasFollower());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, follower.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another permanent");
    }

    @Test
    void canUntapAnotherFollowerYouControl() {
        Permanent source = addCreatureReady(player1, new KiorasFollower());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KiorasFollower());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    void canTargetUntappedPermanentAndUntapItIfTappedBeforeResolution() {
        addCreatureReady(player1, new KiorasFollower());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, null, target.getId());
        target.tap();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new KiorasFollower());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(source.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityStillResolvesAfterSourceLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new KiorasFollower());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotUntapTargetThatLeftBattlefield() {
        Permanent source = addCreatureReady(player1, new KiorasFollower());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(source.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
