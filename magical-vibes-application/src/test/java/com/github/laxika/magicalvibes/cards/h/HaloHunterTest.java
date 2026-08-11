package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HaloHunterTest extends BaseCardTest {

    @Test
    void entersAndDestroysTargetAngel() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new HaloHunter()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID targetId = harness.getPermanentId(player2, "Serra Angel");
        harness.castCreature(player1, 0, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    void cannotTargetNonAngel() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HaloHunter()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Angel");
    }

    @Test
    void doesNotTriggerWhenNoAngelExists() {
        harness.setHand(player1, List.of(new HaloHunter()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Halo Hunter");
        org.assertj.core.api.Assertions.assertThat(harness.getGameData().stack).isEmpty();
    }
}
