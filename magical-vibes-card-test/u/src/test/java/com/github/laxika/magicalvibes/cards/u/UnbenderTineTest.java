package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.q.QasaliPridemage;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnbenderTine.class, QasaliPridemage.class})
class UnbenderTineTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability taps Unbender Tine")
    void activatingTapsUnbenderTine() {
        Permanent unbenderTine = harness.addToBattlefieldAndReturn(player1, new UnbenderTine());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new QasaliPridemage());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(unbenderTine.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps any tapped permanent, not just artifacts")
    void untapsTappedCreature() {
        harness.addToBattlefield(player1, new UnbenderTine());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new QasaliPridemage());
        target.tap();
        assertThat(target.isTapped()).isTrue();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can untap own tapped permanent")
    void canUntapOwnPermanent() {
        harness.addToBattlefield(player1, new UnbenderTine());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new QasaliPridemage());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target itself: must be another permanent")
    void cannotTargetItself() {
        Permanent unbenderTine = harness.addToBattlefieldAndReturn(player1, new UnbenderTine());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, unbenderTine.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another permanent");
    }

    @Test
    @DisplayName("Can untap another copy of Unbender Tine")
    void canUntapAnotherCopy() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new UnbenderTine());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new UnbenderTine());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(target.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An untapped permanent is a legal target")
    void canTargetUntappedPermanent() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new UnbenderTine());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UnbenderTine());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Unbender Tine cannot pay the tap cost")
    void cannotActivateWhileTapped() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new UnbenderTine());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UnbenderTine());
        source.tap();
        target.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(source.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability resolves after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new UnbenderTine());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UnbenderTine());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability does not untap a permanent that left and returned")
    void doesNotUntapReturnedTarget() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new UnbenderTine());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UnbenderTine());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        Permanent returned = harness.addToBattlefieldAndReturn(player2, target.getCard());
        returned.tap();
        harness.passBothPriorities();

        assertThat(returned.isTapped()).isTrue();
        assertThat(source.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
