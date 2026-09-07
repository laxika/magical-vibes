package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CourierHawk.class, GrizzlyBears.class})
class CourierHawkTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking does not tap Courier Hawk")
    void attackingDoesNotTapCourierHawk() {
        Permanent hawk = addCreatureReady(player1, new CourierHawk());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(hawk)));

        assertThat(hawk.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Courier Hawk cannot be blocked by a creature without flying")
    void cannotBeBlockedByGroundCreature() {
        Permanent hawk = addCreatureReady(player1, new CourierHawk());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(hawk)));
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(hawk);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }
}
