package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoldenHind;
import com.github.laxika.magicalvibes.cards.m.MagmaSpray;
import com.github.laxika.magicalvibes.cards.a.AerialFormation;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrainMaggot.class, Forest.class, GoldenHind.class, AerialFormation.class, MagmaSpray.class})
class BrainMaggotTest extends BaseCardTest {

    @Test
    @DisplayName("ETB reveals the opponent's hand and allows choosing any nonland card")
    void etbAllowsChoosingAnyNonlandCard() {
        Card creature = new GoldenHind();
        Card land = new Forest();
        Card instant = new AerialFormation();
        harness.setHand(player2, List.of(creature, land, instant));

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
        Card instant = new AerialFormation();
        harness.setHand(player2, List.of(instant));
        castAndResolveEtb();
        harness.handleCardChosen(player1, 0);

        harness.setHand(player2, List.of(new MagmaSpray()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID brainMaggotId = harness.getPermanentId(player1, "Brain Maggot");

        harness.castAndResolveInstant(player2, 0, brainMaggotId);

        harness.assertNotOnBattlefield(player1, "Brain Maggot");
        harness.assertInHand(player2, "Aerial Formation");
        assertThat(gd.getPlayerExiledCards(player2.getId())).noneMatch(card -> card.getName().equals("Aerial Formation"));
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

    @Test
    @DisplayName("An empty opponent hand produces no choice")
    void emptyHandProducesNoChoice() {
        harness.setHand(player2, List.of());

        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Choosing a nonland is mandatory and a land cannot be chosen")
    void cannotDeclineOrChooseLand() {
        Card land = new Forest();
        Card instant = new AerialFormation();
        harness.setHand(player2, List.of(land, instant));

        castAndResolveEtb();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, instant);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(instant);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("Leaving before the enter trigger resolves does not exile the chosen card")
    void sourceLeavesBeforeTriggerResolves() {
        Card chosen = new AerialFormation();
        harness.setHand(player2, List.of(new MagmaSpray(), chosen));
        harness.setHand(player1, List.of(new BrainMaggot()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        UUID sourceId = harness.getPermanentId(player1, "Brain Maggot");
        harness.castAndResolveInstant(player2, 0, sourceId);
        harness.assertNotOnBattlefield(player1, "Brain Maggot");
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(chosen);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(chosen);
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
