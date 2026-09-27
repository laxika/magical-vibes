package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CourierHawk.class, BorosRecruit.class})
class CourierHawkTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking does not tap Courier Hawk")
    void attackingDoesNotTapCourierHawk() {
        Permanent hawk = addCreatureReady(player1, new CourierHawk());

        declareAttackers(List.of(0));

        assertThat(hawk.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Courier Hawk cannot be blocked by a creature without flying")
    void cannotBeBlockedByGroundCreature() {
        addCreatureReady(player1, new CourierHawk());
        addCreatureReady(player2, new BorosRecruit());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }
}
