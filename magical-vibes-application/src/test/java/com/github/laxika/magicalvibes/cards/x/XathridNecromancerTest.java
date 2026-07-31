package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class XathridNecromancerTest extends BaseCardTest {

    @Test
    @DisplayName("Another Human you control dying creates a tapped 2/2 black Zombie token")
    void createsZombieWhenOtherHumanDies() {
        harness.addToBattlefield(player1, new XathridNecromancer());
        harness.addToBattlefield(player1, new EliteVanguard());

        killWithShock(player2, player1, "Elite Vanguard");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        List<Permanent> zombies = zombieTokens(player1);
        assertThat(zombies).hasSize(1);
        Permanent zombie = zombies.getFirst();
        assertThat(zombie.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Necromancer dying triggers on itself")
    void createsZombieWhenItselfDies() {
        harness.addToBattlefield(player1, new XathridNecromancer());

        killWithShock(player2, player1, "Xathrid Necromancer");

        harness.passBothPriorities();

        assertThat(zombieTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("A non-Human creature you control dying does not trigger")
    void doesNotTriggerForNonHuman() {
        harness.addToBattlefield(player1, new XathridNecromancer());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killWithShock(player2, player1, "Grizzly Bears");

        assertThat(gd.stack).isEmpty();
        assertThat(zombieTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Human dying does not trigger")
    void doesNotTriggerForOpponentHuman() {
        harness.addToBattlefield(player1, new XathridNecromancer());
        harness.addToBattlefield(player2, new EliteVanguard());

        killWithShock(player1, player2, "Elite Vanguard");

        assertThat(gd.stack).isEmpty();
        assertThat(zombieTokens(player1)).isEmpty();
    }

    private void killWithShock(Player caster, Player targetController, String targetName) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }

    private List<Permanent> zombieTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .toList();
    }
}
