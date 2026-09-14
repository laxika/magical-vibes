package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.h.HuntedWumpus;
import com.github.laxika.magicalvibes.cards.j.JhovallRider;
import com.github.laxika.magicalvibes.cards.s.SqueeGoblinNabob;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArmsDealer.class, HuntedWumpus.class, JhovallRider.class, SqueeGoblinNabob.class})
class ArmsDealerTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 4 damage to target creature, sacrificing itself as the Goblin")
    void dealsFourDamageToTargetCreature() {
        harness.addToBattlefield(player1, new ArmsDealer());
        harness.addToBattlefield(player2, new JhovallRider());
        harness.addMana(player1, ManaColor.RED, 2);

        UUID riderId = findPermanent(player2, "Jhovall Rider").getId();

        harness.activateAbility(player1, 0, null, riderId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Arms Dealer");
        harness.assertNotOnBattlefield(player2, "Jhovall Rider");
    }

    @Test
    @DisplayName("Creature with toughness greater than 4 survives the damage")
    void toughCreatureSurvives() {
        harness.addToBattlefield(player1, new ArmsDealer());
        harness.addToBattlefield(player2, new HuntedWumpus());
        harness.addMana(player1, ManaColor.RED, 2);

        UUID wumpusId = findPermanent(player2, "Hunted Wumpus").getId();

        harness.activateAbility(player1, 0, null, wumpusId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hunted Wumpus");
        assertThat(findPermanent(player2, "Hunted Wumpus").getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player1, new ArmsDealer());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can sacrifice another Goblin while Arms Dealer remains on the battlefield")
    void canSacrificeAnotherGoblin() {
        harness.addToBattlefield(player1, new ArmsDealer());
        var squee = harness.addToBattlefieldAndReturn(player1, new SqueeGoblinNabob());
        harness.addToBattlefield(player2, new JhovallRider());
        harness.addMana(player1, ManaColor.RED, 2);

        UUID riderId = findPermanent(player2, "Jhovall Rider").getId();

        harness.activateAbility(player1, 0, null, riderId);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, squee.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Arms Dealer");
        harness.assertInGraveyard(player1, "Squee, Goblin Nabob");
        harness.assertNotOnBattlefield(player2, "Jhovall Rider");
    }

    @Test
    @DisplayName("Only a Goblin can be sacrificed as the activation cost")
    void cannotSacrificeNonGoblin() {
        harness.addToBattlefield(player1, new JhovallRider());
        harness.addToBattlefield(player1, new ArmsDealer());
        harness.addToBattlefield(player2, new HuntedWumpus());
        harness.addMana(player1, ManaColor.RED, 2);

        UUID wumpusId = findPermanent(player2, "Hunted Wumpus").getId();

        harness.activateAbility(player1, 1, null, wumpusId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Arms Dealer");
        harness.assertOnBattlefield(player1, "Jhovall Rider");
        harness.assertOnBattlefield(player2, "Hunted Wumpus");
    }

    @Test
    @DisplayName("Requires {1}{R} mana to activate")
    void requiresActivationMana() {
        harness.addToBattlefield(player1, new ArmsDealer());
        harness.addToBattlefield(player2, new JhovallRider());
        harness.addMana(player1, ManaColor.RED, 1);

        UUID riderId = findPermanent(player2, "Jhovall Rider").getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, riderId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Arms Dealer");
        harness.assertOnBattlefield(player2, "Jhovall Rider");
    }

    @Test
    @DisplayName("Can target a creature controlled by its controller")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new ArmsDealer());
        harness.addToBattlefield(player1, new JhovallRider());
        harness.addMana(player1, ManaColor.RED, 2);

        UUID riderId = findPermanent(player1, "Jhovall Rider").getId();

        harness.activateAbility(player1, 0, null, riderId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Arms Dealer");
        harness.assertNotOnBattlefield(player1, "Jhovall Rider");
    }
}
