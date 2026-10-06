package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.TheFlameOfKeld;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RonaDiscipleOfGix.class, RodOfRuin.class, GrizzlyBears.class, Island.class, TheFlameOfKeld.class})
class RonaDiscipleOfGixTest extends BaseCardTest {

    @Test
    @DisplayName("ETB triggers may prompt when historic card is in graveyard")
    void etbTriggersMayPromptWithHistoricInGraveyard() {
        Card artifact = new RodOfRuin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(artifact)));

        castRonaOnStack(player1);

        // Resolve creature spell
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        // Resolve ETB trigger → may prompt
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting ETB exiles historic card and tracks with source")
    void acceptingETBExilesHistoricCard() {
        Card artifact = new RodOfRuin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(artifact)));

        castRonaOnStack(player1);

        // Resolve creature spell
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        // Resolve ETB trigger → may prompt
        harness.passBothPriorities();

        Permanent rona = findPermanent(player1, "Rona, Disciple of Gix");

        // Accept the may ability → inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        // With only one historic card, it auto-exiles
        // Card should be exiled from graveyard
        harness.assertNotInGraveyard(player1, "Rod of Ruin");

        // Card should be tracked in permanentExiledCards
        List<Card> exiledWithRona = gd.getCardsExiledByPermanent(rona.getId());
        assertThat(exiledWithRona).isNotNull().hasSize(1);
        assertThat(exiledWithRona.getFirst().getName()).isEqualTo("Rod of Ruin");

        // Card should also be in player exiled cards
        assertThat(gd.getPlayerExiledCards(player1.getId()))
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
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        // Resolve ETB trigger → may prompt
        harness.passBothPriorities();
        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        // Card should still be in graveyard
        harness.assertInGraveyard(player1, "Rod of Ruin");
    }

    @Test
    @DisplayName("ETB is not put on the stack without a legal historic target")
    void etbIsNotPutOnStackWithNoHistoricCards() {
        // Only non-historic card in graveyard
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));

        castRonaOnStack(player1);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB trigger → may prompt
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();

        // Bears should still be in graveyard
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

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
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Should be tracked with Rona
        List<Card> exiledWithRona = gd.getCardsExiledByPermanent(rona.getId());
        assertThat(exiledWithRona).isNotNull().hasSize(1);
        assertThat(exiledWithRona.getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Activated ability requires tap (can't use when tapped)")
    void activatedAbilityRequiresTap() {
        Permanent rona = addRonaReady(player1);
        rona.tap();

        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiled card with Rona appears as playable when affordable")
    void exiledCardAppearsPlayable() {
        Permanent rona = addRonaReady(player1);

        // Directly set up an exiled card tracked with Rona
        Card bears = new GrizzlyBears();
        gd.addToExile(player1.getId(), bears, rona.getId());

        // Add mana to cast it
        harness.addMana(player1, ManaColor.GREEN, 2);

        // Verify it can be cast from exile
        harness.castFromExile(player1, bears.getId());
        // Resolve the creature spell from exile
        harness.passBothPriorities();

        // Bears should be on the battlefield
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        // Should be removed from permanentExiledCards
        List<Card> exiledWithRona = gd.getCardsExiledByPermanent(rona.getId());
        assertThat(exiledWithRona).noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot cast exiled card when Rona is not on battlefield")
    void cannotCastWhenRonaGone() {
        Permanent rona = addRonaReady(player1);

        // Set up exiled card tracked with Rona
        Card bears = new GrizzlyBears();
        gd.addToExile(player1.getId(), bears, rona.getId());

        // Remove Rona from battlefield
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.addMana(player1, ManaColor.GREEN, 2);

        // Should not be able to cast — no permission
        assertThatThrownBy(
                () -> harness.castFromExile(player1, bears.getId())
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    @DisplayName("Full flow: ETB exile artifact → activated exile top → cast exiled artifact")
    void fullFlow() {
        Card artifact = new RodOfRuin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(artifact)));

        castRonaOnStack(player1);

        // Resolve creature spell
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        // Resolve ETB trigger → may prompt
        harness.passBothPriorities();

        Permanent rona = findPermanent(player1, "Rona, Disciple of Gix");

        // Accept may ability → inner effect resolves inline (auto-exiles single match)
        harness.handleMayAbilityChosen(player1, true);

        // Verify artifact is exiled with Rona
        assertThat(gd.getCardsExiledByPermanent(rona.getId())).hasSize(1);
        assertThat(gd.getCardsExiledByPermanent(rona.getId()).getFirst().getName()).isEqualTo("Rod of Ruin");

        // Cast the exiled artifact
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, artifact.getId());
        harness.passBothPriorities();

        // Rod of Ruin should be on the battlefield
        harness.assertOnBattlefield(player1, "Rod of Ruin");

        // Should no longer be tracked with Rona
        assertThat(gd.getCardsExiledByPermanent(rona.getId()))
                .noneMatch(c -> c.getName().equals("Rod of Ruin"));
    }

    @Test
    void etbExilesLegendaryNonartifactCard() {
        assertHistoricCardCanBeExiled(new RonaDiscipleOfGix());
    }

    @Test
    void etbExilesNonlegendarySaga() {
        assertHistoricCardCanBeExiled(new TheFlameOfKeld());
    }

    @Test
    void etbCannotTargetOpponentsGraveyard() {
        Card ownArtifact = new RodOfRuin();
        Card opposingArtifact = new RodOfRuin();
        harness.setGraveyard(player1, List.of(ownArtifact));
        harness.setGraveyard(player2, List.of(opposingArtifact));
        castRonaOnStack(player1);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(opposingArtifact.getId()))).isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(ownArtifact.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Rod of Ruin");
        harness.assertNotInGraveyard(player1, "Rod of Ruin");
    }

    @Test
    void etbDoesNotExileAnotherCardWhenTargetLeavesGraveyard() {
        Card target = new RodOfRuin();
        Card other = new RodOfRuin();
        harness.setGraveyard(player1, List.of(target, other));
        castRonaOnStack(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(target);
        gd.addToExile(player1.getId(), target);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.getCardsExiledByPermanent(findPermanent(player1, "Rona, Disciple of Gix").getId())).isEmpty();
    }

    @Test
    void activatedAbilityCannotBeUsedWhileSummoningSick() {
        harness.addToBattlefieldAndReturn(player1, new RonaDiscipleOfGix()).setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatedAbilityWithEmptyLibraryDoesNothing() {
        Permanent rona = addRonaReady(player1);
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rona.isTapped()).isTrue();
        assertThat(gd.getCardsExiledByPermanent(rona.getId())).isEmpty();
    }

    @Test
    void activatedAbilityStillExilesAfterRonaLeavesButNewRonaCannotCastIt() {
        addRonaReady(player1);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();
        addRonaReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("No permission");
    }

    @Test
    void cannotSpendWrongColorToCastExiledSpell() {
        Permanent rona = addRonaReady(player1);
        Card bears = new GrizzlyBears();
        gd.addToExile(player1.getId(), bears, rona.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears);
    }

    @Test
    void cannotCastExiledCreatureOutsideNormalTiming() {
        Permanent rona = addRonaReady(player1);
        Card bears = new GrizzlyBears();
        gd.addToExile(player1.getId(), bears, rona.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromExile(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears);
    }

    @Test
    void cannotPlayLandExiledWithRona() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addRonaReady(player1);
        Card land = new Island();
        harness.setLibrary(player1, List.of(land));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(land);
        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(land);
        harness.assertNotOnBattlefield(player1, "Island");
    }

    private void assertHistoricCardCanBeExiled(Card historic) {
        harness.setGraveyard(player1, List.of(historic));
        castRonaOnStack(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(historic.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(historic);
        assertThat(gd.getCardsExiledByPermanent(findPermanent(player1, "Rona, Disciple of Gix").getId()))
                .containsExactly(historic);
    }

    private void castRonaOnStack(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player, new RonaDiscipleOfGix(), "{1}{U}{B}");
    }

    private Permanent addRonaReady(Player player) {
        return addCreatureReady(player, new RonaDiscipleOfGix());
    }
}
