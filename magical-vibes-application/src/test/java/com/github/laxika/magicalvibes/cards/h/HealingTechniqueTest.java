package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HealingTechnique.class, GrizzlyBears.class})
class HealingTechniqueTest extends BaseCardTest {

    @Test
    void returnsTargetCardGainsLifeAndExilesItself() {
        Card returnedCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returnedCard));
        harness.setHand(player1, List.of(new HealingTechnique()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 10);

        harness.castSorcery(player1, 0, returnedCard.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 12);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Healing Technique"));
    }

    @Test
    void demonstrateMayBeDeclined() {
        Card returnedCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returnedCard));
        harness.setHand(player1, List.of(new HealingTechnique()));
        addMana();

        harness.castSorcery(player1, 0, returnedCard.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
    }

    @Test
    void demonstrateCreatesCopiesForControllerAndChosenOpponent() {
        Card returnedCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returnedCard));
        harness.setHand(player1, List.of(new HealingTechnique()));
        addMana();

        harness.castSorcery(player1, 0, returnedCard.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2)
                .extracting(StackEntry::getControllerId)
                .containsExactlyInAnyOrder(player1.getId(), player2.getId());
    }

    @Test
    void cannotTargetCardInOpponentsGraveyard() {
        Card returnedCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(returnedCard));
        harness.setHand(player1, List.of(new HealingTechnique()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, returnedCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsNoncreatureCardAndGainsItsManaValue() {
        Card returnedCard = new HealingTechnique();
        Card spell = new HealingTechnique();
        harness.setGraveyard(player1, List.of(returnedCard));
        harness.setHand(player1, List.of(spell));
        harness.setLife(player1, 10);
        addMana();

        harness.castSorcery(player1, 0, returnedCard.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(returnedCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 14);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
    }

    @Test
    void illegalTargetPreventsLifeGainAndSelfExile() {
        Card returnedCard = new HealingTechnique();
        Card spell = new HealingTechnique();
        harness.setGraveyard(player1, List.of(returnedCard));
        harness.setHand(player1, List.of(spell));
        harness.setLife(player1, 10);
        addMana();

        harness.castSorcery(player1, 0, returnedCard.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void demonstrateOffersControllerNewTargetsBeforeOpponentChooses() {
        Card originalTarget = new HealingTechnique();
        Card alternateTarget = new HealingTechnique();
        harness.setGraveyard(player1, List.of(originalTarget, alternateTarget));
        harness.setGraveyard(player2, List.of(new HealingTechnique()));
        harness.setHand(player1, List.of(new HealingTechnique()));
        addMana();

        harness.castSorcery(player1, 0, originalTarget.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
