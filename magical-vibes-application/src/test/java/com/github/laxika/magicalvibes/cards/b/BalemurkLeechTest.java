package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DazzlingTheaterPropRoom;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BalemurkLeech.class, GloriousAnthem.class, DazzlingTheaterPropRoom.class})
class BalemurkLeechTest extends BaseCardTest {

    @Test
    void roomEntryAndFullUnlockEachTriggerOnceWithoutGainingLife() {
        harness.addToBattlefield(player1, new BalemurkLeech());
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        int opponentLife = gd.getLife(player2.getId());
        int controllerLife = gd.getLife(player1.getId());

        harness.castModalSorcery(player1, 0, 0, List.of());
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLife);

        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.unlockRoomDoor(player1, 1, 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLife);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerWhenAnOpponentFullyUnlocksARoom() {
        harness.addToBattlefield(player1, new BalemurkLeech());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        int opponentLife = gd.getLife(player2.getId());
        int controllerLife = gd.getLife(player1.getId());

        harness.castModalSorcery(player2, 0, 0, List.of());
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.unlockRoomDoor(player2, 0, 1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLife);
    }

    @Test
    void eachLeechTriggersIndependentlyForAnEnchantmentEntering() {
        harness.addToBattlefield(player1, new BalemurkLeech());
        harness.addToBattlefield(player1, new BalemurkLeech());
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        int opponentLife = gd.getLife(player2.getId());

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eachOpponentLosesLifeWhenAnEnchantmentYouControlEnters() {
        harness.addToBattlefield(player1, new BalemurkLeech());
        int lifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void eachOpponentLosesLifeWhenYouFullyUnlockARoom() {
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new BalemurkLeech());
        int lifeBefore = gd.getLife(player2.getId());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.unlockRoomDoor(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void doesNotTriggerForAnOpponentsEnchantment() {
        harness.addToBattlefield(player1, new BalemurkLeech());
        int lifeBefore = gd.getLife(player2.getId());
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }
}
