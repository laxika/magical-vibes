package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChromaticStar.class, Naturalize.class, Boomerang.class, Plains.class})
class ChromaticStarTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Chromatic Star sacrifices it and immediately prompts for mana color (mana ability)")
    void activateAbilityPromptsManaColorImmediately() {
        harness.addToBattlefield(player1, new ChromaticStar());
        harness.addMana(player1, ManaColor.WHITE, 1);

        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, null, null);

        // The permanent should be sacrificed
        harness.assertNotOnBattlefield(player1, "Chromatic Star");
        harness.assertInGraveyard(player1, "Chromatic Star");

        // The draw trigger waits until the mana ability has finished resolving.
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingManaAbilityTriggers).hasSize(1);
        assertThat(gd.pendingManaAbilityTriggers.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.pendingManaAbilityTriggers.getFirst().getCard().getName()).isEqualTo("Chromatic Star");

        // Should be immediately awaiting color choice (mana ability, no priority pass needed)
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a mana color adds mana to pool immediately")
    void resolveAbilityAddsChosenMana() {
        harness.addToBattlefield(player1, new ChromaticStar());
        harness.addMana(player1, ManaColor.WHITE, 1);

        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, null, null);

        // Mana pool should be empty (1 white was spent on {1} cost)
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        // Choose red mana
        harness.handleListChoice(player1, "RED");

        // Red mana should have been added immediately
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Choosing different mana colors works correctly")
    void chooseDifferentManaColors() {
        for (String color : List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN")) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            player2 = harness.getPlayer2();
            harness.skipMulligan();

            harness.addToBattlefield(player1, new ChromaticStar());
            harness.addMana(player1, ManaColor.WHITE, 1);

            GameData gd = harness.getGameData();
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, null, null);
            // Capture after activation cost is paid
            int manaBefore = gd.playerManaPools.get(player1.getId()).get(manaColor);
            harness.handleListChoice(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                    .isEqualTo(manaBefore + 1);
        }
    }

    @Test
    @DisplayName("Full sequence: mana added immediately, then draw trigger resolves via stack")
    void fullActivationSequenceDrawsCard() {
        harness.addToBattlefield(player1, new ChromaticStar());
        harness.addMana(player1, ManaColor.WHITE, 1);

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        // Activate ability — mana ability resolves immediately, prompts for color
        harness.activateAbility(player1, 0, null, null);

        // Choose mana color — mana added immediately
        harness.handleListChoice(player1, "GREEN");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);

        // The draw trigger should still be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        // Resolve the draw trigger
        harness.passBothPriorities();

        // Player should have drawn a card
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Chromatic Star cannot be activated without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new ChromaticStar());

        // No mana in pool — activation should fail
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                harness.activateAbility(player1, 0, null, null));

        // Chromatic Star should still be on the battlefield
        harness.assertOnBattlefield(player1, "Chromatic Star");
    }

    @Test
    @DisplayName("Sacrificing Star adds mana before its separate draw trigger resolves")
    void sacrificeDoesNotDrawUntilTriggerResolves() {
        harness.addToBattlefield(player1, new ChromaticStar());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Plains(), new Plains()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Plains");
        harness.assertInGraveyard(player1, "Chromatic Star");
    }

    @Test
    @DisplayName("Destroying a tapped Star draws for its controller without adding mana")
    void destructionDrawsForController() {
        var star = harness.addToBattlefieldAndReturn(player1, new ChromaticStar());
        star.tap();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Plains(), new Plains()));
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        harness.castAndResolveInstant(player2, 0, star.getId());

        harness.assertInGraveyard(player1, "Chromatic Star");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Plains");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore - 1);
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
    }

    @Test
    @DisplayName("Returning Star to hand does not trigger a draw")
    void returningToHandDoesNotDraw() {
        var star = harness.addToBattlefieldAndReturn(player1, new ChromaticStar());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Plains()));
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player2, 0, star.getId());

        harness.assertInHand(player1, "Chromatic Star");
        harness.assertNotInGraveyard(player1, "Chromatic Star");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A tapped Star cannot pay its tap cost or be sacrificed for mana")
    void cannotActivateWhileTapped() {
        var star = harness.addToBattlefieldAndReturn(player1, new ChromaticStar());
        star.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Chromatic Star");
        harness.assertNotInGraveyard(player1, "Chromatic Star");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}

