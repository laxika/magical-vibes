package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EliteSpellbinderTest extends BaseCardTest {

    private GrizzlyBears exileBearsFromOpponentHand() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(new EliteSpellbinder()));
        harness.setHand(player2, List.of(bears));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        harness.handleCardChosen(player1, 0);
        return bears;
    }

    @Test
    @DisplayName("May exile a nonland card from the target opponent's hand")
    void exilesChosenNonlandAndGrantsOwnerPermission() {
        GrizzlyBears bears = exileBearsFromOpponentHand();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(bears);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("A spell cast by the exiled card's owner costs {2} more")
    void ownerPaysAdditionalTwoMana() {
        GrizzlyBears bears = exileBearsFromOpponentHand();

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromExile(player2, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castFromExile(player2, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining leaves the opponent's hand unchanged")
    void mayDecline() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(new EliteSpellbinder()));
        harness.setHand(player2, List.of(bears));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(bears);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("A land in the target hand cannot be exiled")
    void cannotExileLand() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new EliteSpellbinder()));
        harness.setHand(player2, List.of(forest));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(forest);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(forest);
    }
}
