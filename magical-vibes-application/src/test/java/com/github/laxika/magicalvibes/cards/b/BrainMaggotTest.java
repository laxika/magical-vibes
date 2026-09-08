package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrainMaggotTest extends BaseCardTest {

    @Test
    @DisplayName("ETB reveals the opponent's hand and allows choosing any nonland card")
    void etbAllowsChoosingAnyNonlandCard() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(creature, land, instant)));

        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0, 2);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, instant);
    }

    @Test
    @DisplayName("Exiled card returns to its owner's hand when Brain Maggot leaves")
    void exiledCardReturnsWhenSourceLeaves() {
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(instant)));
        castAndResolveEtb();
        harness.handleCardChosen(player1, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID brainMaggotId = harness.getPermanentId(player1, "Brain Maggot");

        harness.passPriority(player1);
        harness.castInstant(player2, 0, brainMaggotId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Brain Maggot");
        harness.assertInHand(player2, "Peek");
        assertThat(gd.getPlayerExiledCards(player2.getId())).noneMatch(card -> card.getName().equals("Peek"));
    }

    @Test
    @DisplayName("A hand containing only lands produces no choice")
    void onlyLandsProducesNoChoice() {
        harness.setHand(player2, List.of(new Forest()));

        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Brain Maggot can target only an opponent")
    void cannotTargetItsController() {
        harness.setHand(player1, List.of(new BrainMaggot()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void castAndResolveEtb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BrainMaggot()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
