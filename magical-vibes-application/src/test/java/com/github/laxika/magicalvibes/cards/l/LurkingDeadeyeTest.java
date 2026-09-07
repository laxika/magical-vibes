package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LurkingDeadeye.class, GrizzlyBears.class})
class LurkingDeadeyeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys a creature that was dealt damage this turn")
    void etbDestroysDamagedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.permanentsDealtDamageThisTurn.add(target.getId());

        harness.setHand(player1, List.of(new LurkingDeadeye()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB can destroy your own creature that was dealt damage this turn")
    void etbCanDestroyOwnDamagedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.permanentsDealtDamageThisTurn.add(target.getId());

        harness.setHand(player1, List.of(new LurkingDeadeye()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature that was not dealt damage this turn")
    void cannotTargetUndamagedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new LurkingDeadeye()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }

    @Test
    @DisplayName("Can enter the battlefield when no creature was dealt damage this turn")
    void entersWithoutTargetWhenNoCreatureWasDamaged() {
        harness.setHand(player1, List.of(new LurkingDeadeye()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lurking Deadeye");
        assertThat(gd.stack).isEmpty();
    }
}
