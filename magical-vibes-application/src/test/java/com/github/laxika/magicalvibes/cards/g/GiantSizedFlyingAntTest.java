package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiantSizedFlyingAnt.class, GrizzlyBears.class, Island.class})
class GiantSizedFlyingAntTest extends BaseCardTest {

    @Test
    void tapModeTapsTargetNonlandPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAnt();
        chooseMode("Tap target nonland permanent.");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void untapModeUntapsTargetNonlandPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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

    private void chooseMode(String mode) {
        harness.handleListChoice(player1, mode);
    }

    private void castAnt() {
        harness.setHand(player1, List.of(new GiantSizedFlyingAnt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
