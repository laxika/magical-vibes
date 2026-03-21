package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DiscardUnlessExileCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UrzasTomeTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has activated ability with DrawCardEffect and DiscardUnlessExileCardFromGraveyardEffect")
    void hasCorrectAbility() {
        UrzasTome card = new UrzasTome();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{3}");
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(DrawCardEffect.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(DiscardUnlessExileCardFromGraveyardEffect.class);

        DiscardUnlessExileCardFromGraveyardEffect effect =
                (DiscardUnlessExileCardFromGraveyardEffect) ability.getEffects().get(1);
        assertThat(effect.predicate()).isInstanceOf(CardIsHistoricPredicate.class);
    }

    // ===== No historic card in graveyard — must discard =====

    @Test
    @DisplayName("Without historic card in graveyard, draws then must discard")
    void noHistoricInGraveyard_mustDiscard() {
        harness.addToBattlefield(player1, new UrzasTome());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        // Put a non-historic card in graveyard (creature, not legendary/artifact/saga)
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());

        Card cardInHand = new Shock();
        harness.setHand(player1, List.of(cardInHand));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Should be awaiting discard choice (no historic card to exile)
        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.DISCARD_CHOICE)).isTrue();

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
        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.DISCARD_CHOICE)).isTrue();

        // Hand should have 2 cards now (1 original + 1 drawn)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        // Discard
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    // ===== Historic card in graveyard — player chooses to exile =====

    @Test
    @DisplayName("With historic card in graveyard, can exile it to avoid discarding")
    void historicInGraveyard_exileToAvoidDiscard() {
        harness.addToBattlefield(player1, new UrzasTome());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        // Put an artifact (historic) in graveyard
        Millstone millstone = new Millstone();
        gd.playerGraveyards.get(player1.getId()).add(millstone);

        Card cardInHand = new Shock();
        harness.setHand(player1, List.of(cardInHand));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Should be awaiting may ability choice (exile or discard)
        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.MAY_ABILITY_CHOICE)).isTrue();

        // Hand should have 2 cards now (1 original + 1 drawn)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        // Accept — choose to exile
        harness.handleMayAbilityChosen(player1, true);

        // Should now be awaiting graveyard choice
        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.GRAVEYARD_CHOICE)).isTrue();

        // Choose the Millstone (index 0 in graveyard)
        harness.handleGraveyardCardChosen(player1, 0);

        // Hand should still have 2 cards (no discard needed)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        // Millstone should be exiled
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream()
                .anyMatch(e -> e.card().getName().equals("Millstone"))).isTrue();
    }

    // ===== Historic card in graveyard — player declines exile, must discard =====

    @Test
    @DisplayName("With historic card in graveyard, declining exile requires discard")
    void historicInGraveyard_declineExile_mustDiscard() {
        harness.addToBattlefield(player1, new UrzasTome());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        // Put an artifact (historic) in graveyard
        gd.playerGraveyards.get(player1.getId()).add(new Millstone());

        Card cardInHand = new Shock();
        harness.setHand(player1, List.of(cardInHand));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Should be awaiting may ability choice
        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.MAY_ABILITY_CHOICE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        // Decline — must discard
        harness.handleMayAbilityChosen(player1, false);

        // Should be awaiting discard choice
        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.DISCARD_CHOICE)).isTrue();

        // Discard
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        // Millstone should still be in graveyard (+ the discarded card)
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("Millstone"))).isTrue();
    }

    // ===== Multiple historic cards — only matching shown =====

    @Test
    @DisplayName("Only historic cards in graveyard are valid exile choices")
    void onlyHistoricCardsAreValidExileChoices() {
        harness.addToBattlefield(player1, new UrzasTome());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        // Non-historic creature at index 0
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        // Artifact (historic) at index 1
        Millstone millstone = new Millstone();
        gd.playerGraveyards.get(player1.getId()).add(millstone);

        harness.setHand(player1, List.of(new Shock()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Accept exile
        harness.handleMayAbilityChosen(player1, true);

        // Should be awaiting graveyard choice — only the Millstone (index 1) should be valid
        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.GRAVEYARD_CHOICE)).isTrue();

        // Choose the Millstone at index 1
        harness.handleGraveyardCardChosen(player1, 1);

        // Millstone exiled, GrizzlyBears still in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    // ===== Ability requires tap =====

    @Test
    @DisplayName("Ability requires tap — cannot activate when tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new UrzasTome());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new Shock()));

        harness.activateAbility(player1, 0, null, null);

        boolean isTapped = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Urza's Tome"))
                .anyMatch(p -> p.isTapped());
        assertThat(isTapped).isTrue();
    }
}
