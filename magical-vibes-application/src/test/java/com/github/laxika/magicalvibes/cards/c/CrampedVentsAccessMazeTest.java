package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrampedVentsAccessMaze.class, GrizzlyBears.class})
class CrampedVentsAccessMazeTest extends BaseCardTest {

    @Test
    void crampedVentsDealsSixDamageAndGainsExcessDamageAsLife() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent room = castRoom(0);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(room);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void crampedVentsCannotTargetAControlledCreature() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        castRoom(0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    void accessMazeAllowsOneHandSpellPerTurnByPayingLife() {
        harness.setLife(player1, 20);
        castRoom(1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void accessMazeIsLockedUntilItsDoorIsUnlockedAndLimitedToOncePerTurn() {
        harness.setLife(player1, 20);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castRoom(0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        Permanent room = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ROOM))
                .findFirst().orElseThrow();
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.unlockRoomDoor(player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 1);
        resolveAllTriggers();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new CrampedVentsAccessMaze()));
        harness.addMana(player1, ManaColor.BLACK, doorIndex == 0 ? 4 : 7);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ROOM))
                .findFirst().orElseThrow();
    }
}
