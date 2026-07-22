package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SiegeZombieTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping three untapped creatures makes each opponent lose 1 life")
    void tapThreeCreaturesOpponentsLoseLife() {
        Permanent zombie = addCreatureReady(player1, new SiegeZombie());
        Permanent bearsA = addCreatureReady(player1, new GrizzlyBears());
        Permanent bearsB = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        int idx = indexOf(player1, zombie);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        assertThat(zombie.isTapped()).isTrue();
        assertThat(bearsA.isTapped()).isTrue();
        assertThat(bearsB.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("With more than three creatures, choosing three taps them as cost")
    void choosesThreeOfFourCreatures() {
        Permanent zombie = addCreatureReady(player1, new SiegeZombie());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent spare = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        int idx = indexOf(player1, zombie);
        harness.activateAbility(player1, idx, null, null);
        tapCreatures(player1, 3);
        harness.passBothPriorities();

        assertThat(spare.isTapped()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot activate with fewer than three untapped creatures")
    void cannotActivateWithFewerThanThree() {
        Permanent zombie = addCreatureReady(player1, new SiegeZombie());
        addCreatureReady(player1, new GrizzlyBears());

        int idx = indexOf(player1, zombie);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void tapCreatures(Player player, int count) {
        List<Permanent> untapped = gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> !p.isTapped())
                .limit(count)
                .toList();
        for (Permanent creature : untapped) {
            harness.handlePermanentChosen(player, creature.getId());
        }
    }
}
