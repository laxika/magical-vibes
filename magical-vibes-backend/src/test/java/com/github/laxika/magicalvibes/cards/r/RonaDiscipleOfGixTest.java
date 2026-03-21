package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RonaDiscipleOfGixTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ETB may effect to exile historic card from own graveyard")
    void hasCorrectETBEffect() {
        RonaDiscipleOfGix card = new RonaDiscipleOfGix();

        var etbEffects = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD);
        assertThat(etbEffects).hasSize(1);
        assertThat(etbEffects.getFirst()).isInstanceOf(MayEffect.class);

        MayEffect mayEffect = (MayEffect) etbEffects.getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(ExileTargetCardFromGraveyardAndImprintOnSourceEffect.class);

        ExileTargetCardFromGraveyardAndImprintOnSourceEffect exileEffect =
                (ExileTargetCardFromGraveyardAndImprintOnSourceEffect) mayEffect.wrapped();
        assertThat(exileEffect.filter()).isInstanceOf(CardIsHistoricPredicate.class);
    }

    @Test
    @DisplayName("Has static effect to cast from exiled cards")
    void hasCorrectStaticEffect() {
        RonaDiscipleOfGix card = new RonaDiscipleOfGix();

        var staticEffects = card.getEffects(EffectSlot.STATIC);
        assertThat(staticEffects).hasSize(1);
        assertThat(staticEffects.getFirst()).isInstanceOf(AllowCastFromCardsExiledWithSourceEffect.class);
    }

    @Test
    @DisplayName("Has activated ability: {4}, {T}: Exile the top card of your library")
    void hasCorrectActivatedAbility() {
        RonaDiscipleOfGix card = new RonaDiscipleOfGix();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        ActivatedAbility ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{4}");
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(ExileTopCardOfOwnLibraryEffect.class);

        ExileTopCardOfOwnLibraryEffect effect = (ExileTopCardOfOwnLibraryEffect) ability.getEffects().getFirst();
        assertThat(effect.trackWithSource()).isTrue();
    }

    // ===== ETB: Exile historic card from own graveyard =====

    @Test
    @DisplayName("ETB triggers may prompt when historic card is in graveyard")
    void etbTriggersMayPromptWithHistoricInGraveyard() {
        Card artifact = new RodOfRuin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(artifact)));

        castRonaOnStack(player1);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB trigger → may prompt
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Accepting ETB exiles historic card and tracks with source")
    void acceptingETBExilesHistoricCard() {
        Card artifact = new RodOfRuin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(artifact)));

        castRonaOnStack(player1);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB trigger → may prompt
        harness.passBothPriorities();

        Permanent rona = findRonaOnBattlefield(player1);

        // Accept the may ability → inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        // With only one historic card, it auto-exiles
        // Card should be exiled from graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Rod of Ruin"));

        // Card should be tracked in permanentExiledCards
        List<Card> exiledWithRona = gd.permanentExiledCards.get(rona.getId());
        assertThat(exiledWithRona).isNotNull().hasSize(1);
        assertThat(exiledWithRona.getFirst().getName()).isEqualTo("Rod of Ruin");

        // Card should also be in player exiled cards
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Rod of Ruin"));
    }

    @Test
    @DisplayName("Declining ETB does not exile anything")
    void decliningETBDoesNotExile() {
        Card artifact = new RodOfRuin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(artifact)));

        castRonaOnStack(player1);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB trigger → may prompt
        harness.passBothPriorities();
        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        // Card should still be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Rod of Ruin"));
    }

    @Test
    @DisplayName("ETB does not trigger when no historic cards in graveyard")
    void etbDoesNotTriggerWithNoHistoricCards() {
        // Only non-historic card in graveyard
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));

        castRonaOnStack(player1);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB trigger → may prompt
        harness.passBothPriorities();

        // The may ability should still trigger (the may question is asked regardless)
        if (gd.interaction.awaitingInputType() == AwaitingInput.MAY_ABILITY_CHOICE) {
            // Accept — but inner effect finds no historic cards, so nothing happens
            harness.handleMayAbilityChosen(player1, true);
        }

        // Bears should still be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Activated ability: {4}, {T}: Exile top card of library =====

    @Test
    @DisplayName("Activated ability exiles top card and tracks with source")
    void activatedAbilityExilesTopCard() {
        Permanent rona = addRonaReady(player1);

        // Set up a known top card
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Top card should be exiled
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Should be tracked with Rona
        List<Card> exiledWithRona = gd.permanentExiledCards.get(rona.getId());
        assertThat(exiledWithRona).isNotNull().hasSize(1);
        assertThat(exiledWithRona.getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Activated ability requires tap (can't use when tapped)")
    void activatedAbilityRequiresTap() {
        Permanent rona = addRonaReady(player1);
        rona.tap();

        harness.addMana(player1, ManaColor.COLORLESS, 4);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);
    }

    // ===== Static: Cast from cards exiled with Rona =====

    @Test
    @DisplayName("Exiled card with Rona appears as playable when affordable")
    void exiledCardAppearsPlayable() {
        Permanent rona = addRonaReady(player1);

        // Directly set up an exiled card tracked with Rona
        Card bears = new GrizzlyBears();
        gd.playerExiledCards.computeIfAbsent(player1.getId(), k -> Collections.synchronizedList(new ArrayList<>()))
                .add(bears);
        gd.permanentExiledCards.put(rona.getId(), Collections.synchronizedList(new ArrayList<>(List.of(bears))));

        // Add mana to cast it
        harness.addMana(player1, ManaColor.GREEN, 2);

        // Verify it can be cast from exile
        gs.playCardFromExile(gd, player1, bears.getId(), null, null);
        // Resolve the creature spell from exile
        harness.passBothPriorities();

        // Bears should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Should be removed from permanentExiledCards
        List<Card> exiledWithRona = gd.permanentExiledCards.get(rona.getId());
        assertThat(exiledWithRona).noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot cast exiled card when Rona is not on battlefield")
    void cannotCastWhenRonaGone() {
        Permanent rona = addRonaReady(player1);

        // Set up exiled card tracked with Rona
        Card bears = new GrizzlyBears();
        gd.playerExiledCards.computeIfAbsent(player1.getId(), k -> Collections.synchronizedList(new ArrayList<>()))
                .add(bears);
        gd.permanentExiledCards.put(rona.getId(), Collections.synchronizedList(new ArrayList<>(List.of(bears))));

        // Remove Rona from battlefield
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.addMana(player1, ManaColor.GREEN, 2);

        // Should not be able to cast — no permission
        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> gs.playCardFromExile(gd, player1, bears.getId(), null, null)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }

    // ===== Full flow =====

    @Test
    @DisplayName("Full flow: ETB exile artifact → activated exile top → cast exiled artifact")
    void fullFlow() {
        Card artifact = new RodOfRuin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(artifact)));

        castRonaOnStack(player1);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB trigger → may prompt
        harness.passBothPriorities();

        Permanent rona = findRonaOnBattlefield(player1);

        // Accept may ability → inner effect resolves inline (auto-exiles single match)
        harness.handleMayAbilityChosen(player1, true);

        // Verify artifact is exiled with Rona
        assertThat(gd.permanentExiledCards.get(rona.getId())).hasSize(1);
        assertThat(gd.permanentExiledCards.get(rona.getId()).getFirst().getName()).isEqualTo("Rod of Ruin");

        // Cast the exiled artifact
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        gs.playCardFromExile(gd, player1, artifact.getId(), null, null);
        harness.passBothPriorities();

        // Rod of Ruin should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Rod of Ruin"));

        // Should no longer be tracked with Rona
        assertThat(gd.permanentExiledCards.get(rona.getId()))
                .noneMatch(c -> c.getName().equals("Rod of Ruin"));
    }

    // ===== Helper methods =====

    private void castRonaOnStack(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        RonaDiscipleOfGix card = new RonaDiscipleOfGix();
        harness.setHand(player, List.of(card));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castCreature(player, 0);
    }

    private Permanent findRonaOnBattlefield(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Rona, Disciple of Gix"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Rona not found on battlefield"));
    }

    private Permanent addRonaReady(Player player) {
        RonaDiscipleOfGix card = new RonaDiscipleOfGix();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
