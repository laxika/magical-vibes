package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.ButcherOrgg;
import com.github.laxika.magicalvibes.cards.g.GluttonousZombie;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SliceAndDice.class, GluttonousZombie.class, ButcherOrgg.class})
class SliceAndDiceTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to each creature")
    void dealsFourDamageToEachCreature() {
        harness.addToBattlefield(player1, new GluttonousZombie());
        harness.addToBattlefield(player2, new GluttonousZombie());
        Permanent largeCreature = harness.addToBattlefieldAndReturn(player2, new ButcherOrgg());

        harness.castFromHand(player1, new SliceAndDice(), "{4}{R}{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gluttonous Zombie");
        harness.assertNotOnBattlefield(player2, "Gluttonous Zombie");
        harness.assertOnBattlefield(player2, "Butcher Orgg");
        assertThat(largeCreature.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not deal damage to players")
    void doesNotDealDamageToPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new SliceAndDice(), "{4}{R}{R}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cycling may deal 1 damage to each creature before drawing")
    void cyclingMayDealDamageBeforeDrawing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GluttonousZombie());
        harness.setHand(player1, List.of(new SliceAndDice()));
        harness.setLibrary(player1, List.of(new GluttonousZombie()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Gluttonous Zombie");
        harness.assertInHand(player1, "Gluttonous Zombie");
        harness.assertInGraveyard(player1, "Slice and Dice");
    }

    @Test
    @DisplayName("Declining the cycling damage still draws a card")
    void decliningCyclingDamageStillDraws() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GluttonousZombie());
        harness.setHand(player1, List.of(new SliceAndDice()));
        harness.setLibrary(player1, List.of(new GluttonousZombie()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Gluttonous Zombie");
        harness.assertInHand(player1, "Gluttonous Zombie");
        harness.assertInGraveyard(player1, "Slice and Dice");
    }
}
