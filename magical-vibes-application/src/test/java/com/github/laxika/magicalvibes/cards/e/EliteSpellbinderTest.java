package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EliteSpellbinder.class, GrizzlyBears.class, Forest.class})
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

    @Test
    @DisplayName("Looking at the hand does not publicly reveal its cards")
    void handIsVisibleOnlyToAbilityController() throws Exception {
        List<GameEventEnvelope> events = new ArrayList<>();
        try (AutoCloseable ignored = harness.subscribeToGameEvents(batch -> events.addAll(batch.events()))) {
            exileBearsFromOpponentHand();
        }

        assertThat(events)
                .filteredOn(event -> event.fact() instanceof GameEventFact.PrivateReveal reveal
                        && reveal.zone() == GameEventFact.RevealZone.HAND
                        && reveal.subjectPlayerId().equals(player2.getId()))
                .isNotEmpty()
                .allSatisfy(event -> assertThat(event.audience().playerIds()).containsExactly(player1.getId()));
    }

    @Test
    @DisplayName("The controller looks at the hand even when declining to exile")
    void decliningStillLooksAtHand() throws Exception {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(new EliteSpellbinder()));
        harness.setHand(player2, List.of(bears));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        List<GameEventEnvelope> events = new ArrayList<>();

        try (AutoCloseable ignored = harness.subscribeToGameEvents(batch -> events.addAll(batch.events()))) {
            harness.castCreature(player1, 0, 0, player2.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);
        }

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(bears);
        assertThat(events)
                .filteredOn(event -> event.fact() instanceof GameEventFact.PrivateReveal reveal
                        && reveal.zone() == GameEventFact.RevealZone.HAND
                        && reveal.subjectPlayerId().equals(player2.getId()))
                .isNotEmpty()
                .anySatisfy(event -> {
                    GameEventFact.PrivateReveal reveal = (GameEventFact.PrivateReveal) event.fact();
                    assertThat(reveal.cards()).extracting(GameEventFact.CardSnapshot::cardId)
                            .containsExactly(bears.getId());
                    assertThat(event.audience().playerIds()).containsExactly(player1.getId());
                });
    }

    @Test
    @DisplayName("An empty opponent hand resolves without a card choice")
    void emptyHandDoesNotRequireCardChoice() {
        harness.setHand(player1, List.of(new EliteSpellbinder()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Elite Spellbinder");
    }

}
