package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WildElephant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElephantGraveyard.class, WildElephant.class, GrizzlyBears.class})
class ElephantGraveyardTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless mana adds {C}")
    void tapForColorlessMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new ElephantGraveyard());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration ability grants a shield to target Elephant")
    void regeneratesTargetElephant() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new ElephantGraveyard());
        Permanent elephant = harness.addToBattlefieldAndReturn(player2, new WildElephant());

        harness.activateAbility(player1, 0, 1, null, elephant.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(elephant.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration ability can target an Elephant you control")
    void regeneratesOwnElephant() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new ElephantGraveyard());
        Permanent elephant = harness.addToBattlefieldAndReturn(player1, new WildElephant());

        harness.activateAbility(player1, 0, 1, null, elephant.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(elephant.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration ability cannot target a non-Elephant creature")
    void cannotTargetNonElephant() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new ElephantGraveyard());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an Elephant");
        assertThat(land.isTapped()).isFalse();
        assertThat(creature.getRegenerationShield()).isZero();
    }
}
