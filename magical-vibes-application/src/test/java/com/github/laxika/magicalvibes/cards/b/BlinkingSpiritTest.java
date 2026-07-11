package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BlinkingSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Activating {0} ability puts return-to-hand on the stack")
    void activateAbilityPutsOnStack() {
        harness.addToBattlefield(player1, new BlinkingSpirit());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Activating {0} ability returns Blinking Spirit to owner's hand for free")
    void activateAbilityReturnsToHand() {
        harness.addToBattlefield(player1, new BlinkingSpirit());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blinking Spirit"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Blinking Spirit"));
    }

    @Test
    @DisplayName("Ability can be activated multiple times across re-casts")
    void canActivateMultipleTimesAcrossRecasts() {
        harness.addToBattlefield(player1, new BlinkingSpirit());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blinking Spirit"));

        // Re-cast it
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.WHITE, 4);
        int spiritIndex = -1;
        var hand = gd.playerHands.get(player1.getId());
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals("Blinking Spirit")) {
                spiritIndex = i;
                break;
            }
        }
        gs.playCard(gd, player1, spiritIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Blinking Spirit"));

        // Second bounce
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blinking Spirit"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Blinking Spirit"));
    }
}
