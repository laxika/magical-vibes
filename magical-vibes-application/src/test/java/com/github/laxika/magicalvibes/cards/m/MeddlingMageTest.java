package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Meddling Mage"));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a card name records it on the permanent")
    void choosingNameSetsOnPermanent() {
        harness.setHand(player1, List.of(new MeddlingMage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Grizzly Bears");

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Meddling Mage"))
                .findFirst().orElseThrow();
        assertThat(perm.getChosenName()).isEqualTo("Grizzly Bears");
    }

    // ===== Static casting restriction =====

    @Test
    @DisplayName("Opponent cannot cast spells with the chosen name")
    void opponentCannotCastChosenName() {
        addReadyMeddlingMage(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Controller also cannot cast spells with the chosen name")
    void controllerCannotCastChosenName() {
        addReadyMeddlingMage(player1, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Spells with a different name can still be cast")
    void spellsWithDifferentNamesCanStillBeCast() {
        addReadyMeddlingMage(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new HillGiant()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    // ===== Restriction lifts when source leaves =====

    @Test
    @DisplayName("Casting restriction lifts when Meddling Mage leaves the battlefield")
    void castingRestrictionLiftsWhenSourceLeaves() {
        Permanent mage = addReadyMeddlingMage(player1, "Grizzly Bears");
        gd.playerBattlefields.get(player1.getId()).remove(mage);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    // ===== Helpers =====

    private Permanent addReadyMeddlingMage(Player player, String chosenName) {
        MeddlingMage card = new MeddlingMage();
        Permanent perm = new Permanent(card);
        perm.setChosenName(chosenName);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
