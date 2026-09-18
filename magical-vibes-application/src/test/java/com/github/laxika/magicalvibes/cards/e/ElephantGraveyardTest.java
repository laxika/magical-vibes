package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SouthernElephant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElephantGraveyard.class, SouthernElephant.class, GrizzlyBears.class})
class ElephantGraveyardTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Elephant Graveyard adds colorless mana")
    void tapsForColorlessMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new ElephantGraveyard());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping Elephant Graveyard regenerates a target Elephant")
    void regeneratesTargetElephant() {
        harness.addToBattlefield(player1, new ElephantGraveyard());
        Permanent elephant = harness.addToBattlefieldAndReturn(player2, new SouthernElephant());

        harness.activateAbility(player1, 0, 1, null, elephant.getId());
        harness.passBothPriorities();

        assertThat(elephant.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Elephant Graveyard cannot target a non-Elephant")
    void cannotTargetNonElephant() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new ElephantGraveyard());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an Elephant");
        assertThat(land.isTapped()).isFalse();
        assertThat(bears.getRegenerationShield()).isZero();
    }
}
