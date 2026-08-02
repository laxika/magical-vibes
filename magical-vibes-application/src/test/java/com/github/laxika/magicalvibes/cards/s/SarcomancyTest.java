package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SarcomancyTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates a 2/2 black Zombie token")
    void entersCreatesZombie() {
        harness.setHand(player1, List.of(new Sarcomancy()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Zombie");
        assertThat(token).isNotNull();
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Upkeep trigger does not fire while the Zombie token is around")
    void noDamageWhileZombiePresent() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Sarcomancy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("With no Zombies on the battlefield the upkeep trigger deals 1 damage to you")
    void dealsOneDamageWithoutZombies() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Sarcomancy());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("An opponent's Zombie also stops the upkeep trigger")
    void opponentZombieStopsTrigger() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Sarcomancy());
        harness.setHand(player2, List.of(new Sarcomancy()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }
}
