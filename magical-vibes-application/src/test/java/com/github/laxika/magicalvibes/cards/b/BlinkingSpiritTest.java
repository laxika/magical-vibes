package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ControlMagic;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlinkingSpirit.class, ControlMagic.class})
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

        harness.assertInHand(player1, "Blinking Spirit");
        harness.assertNotOnBattlefield(player1, "Blinking Spirit");
    }

    @Test
    @DisplayName("Ability can be activated multiple times across re-casts")
    void canActivateMultipleTimesAcrossRecasts() {
        harness.addToBattlefield(player1, new BlinkingSpirit());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Blinking Spirit");

        // Re-cast it
        var spiritCard = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Blinking Spirit"))
                .findFirst()
                .orElseThrow();
        harness.castFromHand(player1, spiritCard, "{3}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Blinking Spirit");

        // Second bounce
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Blinking Spirit");
        harness.assertNotOnBattlefield(player1, "Blinking Spirit");
    }

    @Test
    @DisplayName("Returns to its owner's hand when controlled by another player")
    void returnsToOwnersHandWhenControlledByAnotherPlayer() {
        var spirit = harness.addToBattlefieldAndReturn(player1, new BlinkingSpirit());

        harness.setHand(player2, List.of(new ControlMagic()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castEnchantment(player2, 0, spirit.getId());
        harness.passBothPriorities();

        int spiritIndex = gd.playerBattlefields.get(player2.getId()).indexOf(spirit);
        harness.activateAbility(player2, spiritIndex, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Blinking Spirit");
        harness.assertNotInHand(player2, "Blinking Spirit");
        harness.assertNotOnBattlefield(player2, "Blinking Spirit");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Spirit can return during an opponent's turn")
    void canActivateWhileTappedAndSummoningSickOnOpponentsTurn() {
        var spirit = harness.addToBattlefieldAndReturn(player1, new BlinkingSpirit());
        spirit.tap();
        spirit.setSummoningSick(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Blinking Spirit");
        harness.assertNotOnBattlefield(player1, "Blinking Spirit");
    }

    @Test
    @DisplayName("Multiple activations return the source only once and leave other Spirits alone")
    void repeatedActivationsDoNotReturnAnotherSpirit() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new BlinkingSpirit());
        var otherSpirit = harness.addToBattlefieldAndReturn(player1, new BlinkingSpirit());

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(otherSpirit);
    }

    @Test
    @DisplayName("An older activation cannot return the source after it leaves and reenters")
    void olderActivationDoesNotReturnNewPermanent() {
        var card = new BlinkingSpirit();
        var original = harness.addToBattlefieldAndReturn(player1, card);
        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Blinking Spirit");

        harness.setHand(player1, List.of());
        var returned = harness.addToBattlefieldAndReturn(player1, card);
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(returned);
        harness.assertNotInHand(player1, "Blinking Spirit");
    }
}
