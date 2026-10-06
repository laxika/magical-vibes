package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SiegeZombie.class, GrizzlyBears.class})
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

    @Test
    @DisplayName("Summoning-sick creatures, including Siege Zombie, can pay the cost")
    void summoningSickCreaturesCanPayCost() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new SiegeZombie());
        Permanent otherA = harness.addToBattlefieldAndReturn(player1, new SiegeZombie());
        Permanent otherB = harness.addToBattlefieldAndReturn(player1, new SiegeZombie());
        zombie.setSummoningSick(true);
        otherA.setSummoningSick(true);
        otherB.setSummoningSick(true);

        harness.activateAbility(player1, indexOf(player1, zombie), null, null);

        assertThat(zombie.isTapped()).isTrue();
        assertThat(otherA.isTapped()).isTrue();
        assertThat(otherB.isTapped()).isTrue();
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An already-tapped Siege Zombie can activate using three other creatures")
    void tappedSourceCanActivateUsingOtherCreatures() {
        Permanent zombie = addCreatureReady(player1, new SiegeZombie());
        zombie.tap();
        Permanent otherA = addCreatureReady(player1, new SiegeZombie());
        Permanent otherB = addCreatureReady(player1, new SiegeZombie());
        Permanent otherC = addCreatureReady(player1, new SiegeZombie());

        harness.activateAbility(player1, indexOf(player1, zombie), null, null);
        harness.passBothPriorities();

        assertThat(otherA.isTapped()).isTrue();
        assertThat(otherB.isTapped()).isTrue();
        assertThat(otherC.isTapped()).isTrue();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Tapped creatures cannot count toward the three-creature cost")
    void tappedCreaturesCannotPayCost() {
        Permanent zombie = addCreatureReady(player1, new SiegeZombie());
        addCreatureReady(player1, new SiegeZombie());
        Permanent tapped = addCreatureReady(player1, new SiegeZombie());
        tapped.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, zombie), null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(zombie.isTapped()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent's creatures cannot pay the activation cost")
    void opponentsCreaturesCannotPayCost() {
        Permanent zombie = addCreatureReady(player1, new SiegeZombie());
        addCreatureReady(player1, new SiegeZombie());
        Permanent opponentCreature = addCreatureReady(player2, new SiegeZombie());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, zombie), null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(zombie.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isFalse();
        harness.assertLife(player2, 20);
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
