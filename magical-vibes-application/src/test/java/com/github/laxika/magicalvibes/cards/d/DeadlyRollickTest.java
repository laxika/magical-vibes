package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadlyRollick.class, GrizzlyBears.class, Forest.class})
class DeadlyRollickTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for free while its controller controls a commander")
    void castsForFreeWithCommander() {
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        commander.setCommander(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeadlyRollick()));

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Cannot use the free cast without controlling a commander")
    void cannotCastForFreeWithoutCommander() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeadlyRollick()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target only a creature")
    void rejectsNonCreatureTarget() {
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        commander.setCommander(true);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new DeadlyRollick()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, forest.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
