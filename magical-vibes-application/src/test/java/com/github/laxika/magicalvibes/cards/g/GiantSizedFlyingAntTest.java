package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiantSizedFlyingAnt.class, Island.class})
class GiantSizedFlyingAntTest extends BaseCardTest {

    @Test
    void tapModeTapsTargetNonlandPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSizedFlyingAnt());

        castAnt();
        chooseMode("Tap target nonland permanent.");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void untapModeUntapsTargetNonlandPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSizedFlyingAnt());
        target.tap();

        castAnt();
        chooseMode("Untap target nonland permanent.");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void modesCannotTargetLands() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());

        castAnt();
        chooseMode("Tap target nonland permanent.");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void untapModeCannotTargetLands() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        target.tap();

        castAnt();
        chooseMode("Untap target nonland permanent.");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tapModeCanTargetAlreadyTappedPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSizedFlyingAnt());
        target.tap();

        castAnt();
        chooseMode("Tap target nonland permanent.");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void untapModeCanTargetAlreadyUntappedPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GiantSizedFlyingAnt());

        castAnt();
        chooseMode("Untap target nonland permanent.");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringAntCanTargetItself() {
        castAnt();
        Permanent ant = gd.playerBattlefields.get(player1.getId()).getFirst();
        chooseMode("Tap target nonland permanent.");
        harness.handlePermanentChosen(player1, ant.getId());

        assertThat(ant.isTapped()).isFalse();
        harness.passBothPriorities();

        assertThat(ant.isTapped()).isTrue();
    }

    private void chooseMode(String mode) {
        harness.handleListChoice(player1, mode);
    }

    private void castAnt() {
        harness.castFromHand(player1, new GiantSizedFlyingAnt(), "{3}{U}");
        harness.passBothPriorities();
    }
}
