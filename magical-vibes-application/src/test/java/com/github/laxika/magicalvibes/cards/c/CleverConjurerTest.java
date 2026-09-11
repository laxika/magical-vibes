package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CleverConjurer.class, Forest.class})
class CleverConjurerTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps a target permanent not named Clever Conjurer")
    void untapsTargetPermanent() {
        Permanent conjurer = addReadyConjurer();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(conjurer.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a permanent named Clever Conjurer")
    void cannotTargetCleverConjurer() {
        addReadyConjurer();
        Permanent otherConjurer = addCreatureReady(player1, new CleverConjurer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherConjurer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void requiresSorcerySpeed() {
        Permanent conjurer = addReadyConjurer();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(conjurer.isTapped()).isFalse();
    }

    private Permanent addReadyConjurer() {
        return addCreatureReady(player1, new CleverConjurer());
    }
}
