package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LanternSpirit.class})
class LanternSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("A Lantern Spirit controlled by another player returns to its owner")
    void returnsToOwnerRatherThanController() {
        var card = new LanternSpirit();
        card.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, card);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Lantern Spirit");
        harness.assertNotInHand(player1, "Lantern Spirit");
        harness.assertNotOnBattlefield(player1, "Lantern Spirit");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Lantern Spirit can return itself")
    void canActivateWhileTappedAndSummoningSick() {
        var spirit = harness.addToBattlefieldAndReturn(player1, new LanternSpirit());
        spirit.tap();
        spirit.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Lantern Spirit");
        harness.assertNotOnBattlefield(player1, "Lantern Spirit");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("An older activation cannot return a new permanent representing the same card")
    void olderActivationDoesNotReturnNewPermanent() {
        var card = new LanternSpirit();
        var original = harness.addToBattlefieldAndReturn(player1, card);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();
        harness.assertInHand(player1, "Lantern Spirit");
        assertThat(gd.stack).hasSize(1);

        gd.playerHands.get(player1.getId()).remove(card);
        var returned = harness.addToBattlefieldAndReturn(player1, card);
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Lantern Spirit");
        harness.assertNotInHand(player1, "Lantern Spirit");
    }

    @Test
    @DisplayName("Casting Lantern Spirit puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new LanternSpirit(), "{2}{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(LanternSpirit.class);
    }

    @Test
    @DisplayName("Resolving Lantern Spirit puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new LanternSpirit(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Lantern Spirit");
    }

    @Test
    @DisplayName("Activating {U} ability puts return-to-hand on the stack")
    void activateAbilityPutsOnStack() {
        harness.addToBattlefield(player1, new LanternSpirit());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Activating {U} ability returns Lantern Spirit to owner's hand")
    void activateAbilityReturnsToHand() {
        harness.addToBattlefield(player1, new LanternSpirit());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Lantern Spirit");
        harness.assertNotOnBattlefield(player1, "Lantern Spirit");
    }

    @Test
    @DisplayName("Lantern Spirit can be re-cast after returning to hand")
    void canRecastAfterBounce() {
        harness.addToBattlefield(player1, new LanternSpirit());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Lantern Spirit");

        // Re-cast it
        harness.addMana(player1, ManaColor.BLUE, 3);
        int spiritIndex = -1;
        var hand = gd.playerHands.get(player1.getId());
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals("Lantern Spirit")) {
                spiritIndex = i;
                break;
            }
        }
        harness.castCreature(player1, spiritIndex);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lantern Spirit");
    }

    @Test
    @DisplayName("Ability can be activated multiple times across re-casts")
    void canActivateMultipleTimesAcrossRecasts() {
        harness.addToBattlefield(player1, new LanternSpirit());

        // First bounce
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Lantern Spirit");

        // Re-cast
        harness.addMana(player1, ManaColor.BLUE, 3);
        int spiritIndex = -1;
        var hand = gd.playerHands.get(player1.getId());
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals("Lantern Spirit")) {
                spiritIndex = i;
                break;
            }
        }
        harness.castCreature(player1, spiritIndex);
        harness.passBothPriorities();

        // Second bounce
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Lantern Spirit");
        harness.assertNotOnBattlefield(player1, "Lantern Spirit");
    }
}
