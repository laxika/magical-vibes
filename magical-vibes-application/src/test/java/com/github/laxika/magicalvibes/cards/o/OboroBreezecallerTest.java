package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OboroBreezecallerTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a land as a cost and untaps the target land")
    void returnsLandAndUntapsTargetLand() {
        harness.addToBattlefield(player1, new OboroBreezecaller());
        harness.addToBattlefield(player1, new Island());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Island");
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without a land to return")
    void cannotActivateWithoutLandToReturn() {
        harness.addToBattlefield(player1, new OboroBreezecaller());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player1, new OboroBreezecaller());
        harness.addToBattlefield(player1, new Island());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
        assertThat(target.isTapped()).isTrue();
    }
}
