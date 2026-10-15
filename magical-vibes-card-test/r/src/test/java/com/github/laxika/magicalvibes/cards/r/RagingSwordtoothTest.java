package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BishopsSoldier;
import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RagingSwordtooth.class, BishopsSoldier.class, JungleDelver.class})
class RagingSwordtoothTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack as a creature spell")
    void castingPutsOnStack() {
        castSwordtooth();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Raging Swordtooth");
    }

    @Test
    @DisplayName("ETB deals 1 damage to opponent's 1/1 creature, killing it")
    void etbKillsOpponentOneOne() {
        harness.addToBattlefield(player2, new JungleDelver());

        castSwordtooth();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Jungle Delver");
        harness.assertInGraveyard(player2, "Jungle Delver");
    }

    @Test
    @DisplayName("ETB deals 1 damage to controller's own 1/1 creature, killing it")
    void etbKillsControllerOneOne() {
        harness.addToBattlefield(player1, new JungleDelver());

        castSwordtooth();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Jungle Delver");
    }

    @Test
    @DisplayName("ETB does NOT deal damage to itself")
    void etbDoesNotDamageItself() {
        castSwordtooth();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Raging Swordtooth");
        // Swordtooth is 5/5 — even if it took 1 damage it would survive,
        // but verify it has no damage at all
        assertThat(findPermanent(player1, "Raging Swordtooth").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("ETB damages creatures on both sides but not itself")
    void etbDamagesBothSidesExceptSelf() {
        harness.addToBattlefield(player1, new BishopsSoldier()); // 2/2 own
        harness.addToBattlefield(player2, new BishopsSoldier()); // 2/2 opponent

        castSwordtooth();
        resolveAllTriggers();

        // Both soldiers survive (2/2 take 1 damage) but have 1 damage
        assertThat(findPermanent(player1, "Bishop's Soldier").getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanent(player2, "Bishop's Soldier").getMarkedDamage()).isEqualTo(1);
        // Swordtooth itself has no damage
        assertThat(findPermanent(player1, "Raging Swordtooth").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("ETB does not deal damage to players")
    void etbDoesNotDamagePlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castSwordtooth();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB damages another Raging Swordtooth but excludes only its own source")
    void etbDamagesAnotherSwordtooth() {
        var other = harness.addToBattlefieldAndReturn(player2, new RagingSwordtooth());

        castSwordtooth();
        resolveAllTriggers();

        assertThat(other.getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanent(player1, "Raging Swordtooth").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("ETB damages creatures that enter before the trigger resolves")
    void etbUsesCreaturesPresentAtResolution() {
        castSwordtooth();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player2, new JungleDelver());

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Jungle Delver");
        harness.assertInGraveyard(player2, "Jungle Delver");
    }

    @Test
    @DisplayName("Entering without being cast still triggers damage")
    void enteringWithoutCastingDealsDamage() {
        harness.addToBattlefield(player2, new JungleDelver());

        var source = harness.enterBattlefieldAndReturn(player1, new RagingSwordtooth());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Jungle Delver");
        assertThat(source.getMarkedDamage()).isZero();
    }

    private void castSwordtooth() {
        harness.castFromHand(player1, new RagingSwordtooth(), "{3}{R}{G}");
    }
}
