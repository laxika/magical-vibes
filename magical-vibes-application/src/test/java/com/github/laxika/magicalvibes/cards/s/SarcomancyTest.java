package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.z.ZombieInfestation;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Sarcomancy.class)
class SarcomancyTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates a 2/2 black Zombie token")
    void entersCreatesZombie() {
        harness.castFromHand(player1, new Sarcomancy(), "{B}");
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Zombie");
        assertThat(token).isNotNull();
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("Upkeep trigger does not fire while the Zombie token is around")
    void noDamageWhileZombiePresent() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new Sarcomancy(), "{B}");
        resolveAllTriggers();

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("With no Zombies on the battlefield the upkeep trigger deals 1 damage to you")
    void dealsOneDamageWithoutZombies() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Sarcomancy());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent's Zombie also stops the upkeep trigger")
    void opponentZombieStopsTrigger() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Sarcomancy());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new Sarcomancy(), "{B}");
        resolveAllTriggers();

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The upkeep trigger only fires during the controller's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Sarcomancy());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @CardUsed(ZombieInfestation.class)
    @DisplayName("A Zombie appearing before resolution stops the upkeep damage")
    void rechecksZombieConditionBeforeResolution() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Sarcomancy());
        Permanent infestation = harness.addToBattlefieldAndReturn(player1, new ZombieInfestation());
        harness.setHand(player1, List.of(new Sarcomancy(), new Sarcomancy()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        int infestationIndex = gd.playerBattlefields.get(player1.getId()).indexOf(infestation);
        harness.activateAbility(player1, infestationIndex, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
    }
}
