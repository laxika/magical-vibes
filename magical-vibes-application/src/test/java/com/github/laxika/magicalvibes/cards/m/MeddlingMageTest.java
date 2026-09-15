package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DromarsCavern;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MeddlingMage.class, MoggSentry.class, MoggJailer.class, DromarsCavern.class})
class MeddlingMageTest extends BaseCardTest {

    // ===== Enters-the-battlefield card name choice =====

    @Test
    @DisplayName("Resolving Meddling Mage awaits card name choice before entering battlefield")
    void resolvingAwaitsCardNameChoice() {
        harness.setHand(player1, List.of(new MeddlingMage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Meddling Mage");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a card name records it on the permanent")
    void choosingNameSetsOnPermanent() {
        harness.setHand(player1, List.of(new MeddlingMage()));
        harness.setHand(player2, List.of(new MoggSentry()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Mogg Sentry");

        Permanent perm = findPermanent(player1, "Meddling Mage");
        assertThat(perm.getChosenName()).isEqualTo("Mogg Sentry");
    }

    @Test
    @DisplayName("Card name choice excludes land names")
    void cardNameChoiceExcludesLandNames() {
        harness.setHand(player1, List.of(new MeddlingMage()));
        harness.setHand(player2, List.of(new MoggSentry(), new DromarsCavern()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).contains("Mogg Sentry").doesNotContain("Dromar's Cavern");

        harness.handleListChoice(player1, "Mogg Sentry");
    }

    // ===== Static casting restriction =====

    @Test
    @DisplayName("Opponent cannot cast spells with the chosen name")
    void opponentCannotCastChosenName() {
        addReadyMeddlingMage(player1, "Mogg Sentry");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new MoggSentry()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Controller also cannot cast spells with the chosen name")
    void controllerCannotCastChosenName() {
        addReadyMeddlingMage(player1, "Mogg Sentry");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MoggSentry()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Spells with a different name can still be cast")
    void spellsWithDifferentNamesCanStillBeCast() {
        addReadyMeddlingMage(player1, "Mogg Sentry");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new MoggJailer()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    // ===== Restriction lifts when source leaves =====

    @Test
    @DisplayName("Casting restriction lifts when Meddling Mage leaves the battlefield")
    void castingRestrictionLiftsWhenSourceLeaves() {
        Permanent mage = addReadyMeddlingMage(player1, "Mogg Sentry");
        gd.playerBattlefields.get(player1.getId()).remove(mage);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new MoggSentry()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    // ===== Helpers =====

    private Permanent addReadyMeddlingMage(Player player, String chosenName) {
        Permanent perm = addCreatureReady(player, new MeddlingMage());
        perm.setChosenName(chosenName);
        return perm;
    }
}
