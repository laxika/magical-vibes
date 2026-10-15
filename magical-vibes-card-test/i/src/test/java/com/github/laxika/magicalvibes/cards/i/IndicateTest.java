package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Indicate.class, Island.class})
class IndicateTest extends BaseCardTest {

    @Test
    void targetsAnyPermanentAndResolvesWithoutChangingIt() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new Indicate()));

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Island");
        harness.assertInGraveyard(player1, "Indicate");
    }

    @Test
    void cannotTargetAPlayer() {
        harness.setHand(player1, List.of(new Indicate()));

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot target players");
    }

    @Test
    void canTargetItsControllersPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Island());
        target.tap();
        harness.setHand(player1, List.of(new Indicate()));

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player1, "Island");
        assertThat(target.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Indicate");
    }

    @Test
    void cannotBeCastWithoutATarget() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new Indicate()));

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Indicate");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void goesToGraveyardWhenItsTargetLeavesTheBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new Indicate()));
        harness.castSorcery(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setHand(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInHand(player2, "Island");
        harness.assertInGraveyard(player1, "Indicate");
        assertThat(gd.stack).isEmpty();
    }
}
