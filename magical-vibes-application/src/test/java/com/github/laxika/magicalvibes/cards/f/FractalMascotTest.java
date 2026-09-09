package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FractalMascotTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps target opposing creature and puts a stun counter on it")
    void etbTapsAndStunsTargetOpposingCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFractalMascot(bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        UUID ownCreatureId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new FractalMascot()));
        addManaForFractalMascot();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownCreatureId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castFractalMascot(UUID targetId) {
        harness.setHand(player1, List.of(new FractalMascot()));
        addManaForFractalMascot();
        harness.castCreature(player1, 0, 0, targetId);
    }

    private void addManaForFractalMascot() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
