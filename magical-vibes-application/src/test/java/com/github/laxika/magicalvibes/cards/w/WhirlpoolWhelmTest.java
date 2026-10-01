package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowDodger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhirlpoolWhelm.class, GoldmeadowDodger.class, Forest.class})
class WhirlpoolWhelmTest extends BaseCardTest {

    private UUID prepare() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player2, new GoldmeadowDodger()); // the bounce target
        harness.setHand(player1, List.of(new WhirlpoolWhelm()));
        harness.addMana(player1, ManaColor.BLUE, 2); // {1}{U}

        return harness.getPermanentId(player2, "Goldmeadow Dodger");
    }

    // Caster (player1) wins the clash: their revealed top card has a strictly greater mana value.
    private void stackClashWinForCaster() {
        harness.setLibrary(player1, List.of(new GoldmeadowDodger(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
    }

    // Caster (player1) loses the clash: the opponent reveals the higher mana value.
    private void stackClashLossForCaster() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new GoldmeadowDodger(), new Forest(), new Forest()));
    }

    // Neither player wins when the revealed cards have the same mana value.
    private void stackClashTie() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
    }

    @Test
    @DisplayName("Winning the clash and accepting puts the creature on top of its owner's library")
    void wonClashAcceptPutsOnTopOfLibrary() {
        UUID targetId = prepare();
        stackClashWinForCaster();

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Won clash → controller is offered the "put on top instead" choice.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Goldmeadow Dodger");
        harness.assertNotInHand(player2, "Goldmeadow Dodger");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Goldmeadow Dodger");
    }

    @Test
    @DisplayName("Winning the clash and declining returns the creature to its owner's hand")
    void wonClashDeclineReturnsToHand() {
        UUID targetId = prepare();
        stackClashWinForCaster();

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player2, "Goldmeadow Dodger");
        harness.assertInHand(player2, "Goldmeadow Dodger");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isNotEqualTo("Goldmeadow Dodger");
    }

    @Test
    @DisplayName("Losing the clash returns the creature to its owner's hand with no choice offered")
    void lostClashReturnsToHand() {
        UUID targetId = prepare();
        stackClashLossForCaster();

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // No "put on top" choice on a loss — the creature simply goes to hand.
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player2, "Goldmeadow Dodger");
        harness.assertInHand(player2, "Goldmeadow Dodger");
    }

    @Test
    @DisplayName("Tied clash returns the creature to its owner's hand with no choice offered")
    void tiedClashReturnsToHand() {
        UUID targetId = prepare();
        stackClashTie();

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player2, "Goldmeadow Dodger");
        harness.assertInHand(player2, "Goldmeadow Dodger");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new GoldmeadowDodger()); // valid target so spell is playable
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new WhirlpoolWhelm()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID landId = harness.getPermanentId(player2, "Forest");

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
