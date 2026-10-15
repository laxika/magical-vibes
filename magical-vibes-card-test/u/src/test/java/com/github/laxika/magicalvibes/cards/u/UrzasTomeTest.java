package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.k.KarnScionOfUrza;
import com.github.laxika.magicalvibes.cards.t.TheAntiquitiesWar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrzasTome.class, GrizzlyBears.class, Millstone.class, Shock.class,
        KarnScionOfUrza.class, TheAntiquitiesWar.class})
class UrzasTomeTest extends BaseCardTest {

    @Test
    @DisplayName("Without historic card in graveyard, draws then must discard")
    void noHistoricInGraveyard_mustDiscard() {
        harness.addToBattlefield(player1, new UrzasTome());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        // Put a non-historic card in graveyard (creature, not legendary/artifact/saga)
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        Card cardInHand = new Shock();
        harness.setHand(player1, List.of(cardInHand));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Should be awaiting discard choice (no historic card to exile)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class) != null).isTrue();

        // Hand should have 2 cards now (1 original + 1 drawn)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        // Choose to discard the first card
        harness.handleCardChosen(player1, 0);

        // After discard, hand should be back to 1
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("With empty graveyard, draws then must discard")
    void emptyGraveyard_mustDiscard() {
        harness.addToBattlefield(player1, new UrzasTome());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Card cardInHand = new Shock();
        harness.setHand(player1, List.of(cardInHand));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Should be awaiting discard choice
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class) != null).isTrue();

        // Hand should have 2 cards now (1 original + 1 drawn)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        // Discard
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("With historic card in graveyard, can exile it to avoid discarding")
    void historicInGraveyard_exileToAvoidDiscard() {
        harness.addToBattlefield(player1, new UrzasTome());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        // Put an artifact (historic) in graveyard
        Millstone millstone = new Millstone();
        harness.setGraveyard(player1, List.of(millstone));

        Card cardInHand = new Shock();
        harness.setHand(player1, List.of(cardInHand));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Should be awaiting may ability choice (exile or discard)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null).isTrue();

        // Hand should have 2 cards now (1 original + 1 drawn)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        // Accept — choose to exile
        harness.handleMayAbilityChosen(player1, true);

        // Should now be awaiting graveyard choice
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class) != null).isTrue();

        // Choose the Millstone (index 0 in graveyard)
        harness.handleGraveyardCardChosen(player1, 0);

        // Hand should still have 2 cards (no discard needed)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        // Millstone should be exiled
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream()
                .anyMatch(e -> e.card().getName().equals("Millstone"))).isTrue();
    }

    @Test
    @DisplayName("With historic card in graveyard, declining exile requires discard")
    void historicInGraveyard_declineExile_mustDiscard() {
        harness.addToBattlefield(player1, new UrzasTome());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        // Put an artifact (historic) in graveyard
        harness.setGraveyard(player1, List.of(new Millstone()));

        Card cardInHand = new Shock();
        harness.setHand(player1, List.of(cardInHand));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Should be awaiting may ability choice
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        // Decline — must discard
        harness.handleMayAbilityChosen(player1, false);

        // Should be awaiting discard choice
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class) != null).isTrue();

        // Discard
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        // Millstone should still be in graveyard (+ the discarded card)
        harness.assertInGraveyard(player1, "Millstone");
    }

    @Test
    @DisplayName("Only historic cards in graveyard are valid exile choices")
    void onlyHistoricCardsAreValidExileChoices() {
        harness.addToBattlefield(player1, new UrzasTome());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        Millstone millstone = new Millstone();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), millstone));

        harness.setHand(player1, List.of(new Shock()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Accept exile
        harness.handleMayAbilityChosen(player1, true);

        // Should be awaiting graveyard choice — only the Millstone (index 1) should be valid
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class) != null).isTrue();

        // Choose the Millstone at index 1
        harness.handleGraveyardCardChosen(player1, 1);

        // Millstone exiled, GrizzlyBears still in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Ability requires tap — cannot activate when tapped")
    void cannotActivateWhenTapped() {
        var tome = harness.addToBattlefieldAndReturn(player1, new UrzasTome());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new Shock()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(tome.isTapped()).isTrue();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void legendaryNonartifactCanBeExiled() {
        assertHistoricCanBeExiled(new KarnScionOfUrza());
    }

    @Test
    void nonlegendarySagaCanBeExiled() {
        assertHistoricCanBeExiled(new TheAntiquitiesWar());
    }

    private void assertHistoricCanBeExiled(Card historic) {
        harness.addToBattlefield(player1, new UrzasTome());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new UrzasTome()));
        harness.setGraveyard(player1, List.of(historic));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(historic.getId()));
    }

    @Test
    void opponentsHistoricCardCannotAvoidDiscard() {
        harness.addToBattlefield(player1, new UrzasTome());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of());
        UrzasTome drawn = new UrzasTome();
        harness.setLibrary(player1, List.of(drawn));
        UrzasTome opponentCard = new UrzasTome();
        harness.setGraveyard(player2, List.of(opponentCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void cannotActivateWithLessThanThreeMana() {
        var tome = harness.addToBattlefieldAndReturn(player1, new UrzasTome());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tome.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
