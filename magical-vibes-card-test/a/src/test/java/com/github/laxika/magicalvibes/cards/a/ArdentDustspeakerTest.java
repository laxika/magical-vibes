package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Duress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArdentDustspeaker.class, Duress.class, GrizzlyBears.class, LlanowarElves.class, Shock.class})
class ArdentDustspeakerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking can put an instant or sorcery on the bottom and exile the top two cards")
    void attackPutsSpellOnBottomAndExilesTopTwo() {
        Card instant = new Shock();
        Card sorcery = new Duress();
        Card nonMatching = new GrizzlyBears();
        Card topCard = new LlanowarElves();
        Card secondCard = new GrizzlyBears();
        Card cardBelowExiledCards = new GrizzlyBears();
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(instant, sorcery, nonMatching));
        harness.setLibrary(player1, List.of(topCard, secondCard, cardBelowExiledCards));
        addCreatureReady(player1, new ArdentDustspeaker());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(instant.getId(), sorcery.getId());
        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant, nonMatching);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(cardBelowExiledCards, sorcery);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(topCard, secondCard);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(topCard.getId(), player1.getId())
                .containsEntry(secondCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn)
                .contains(topCard.getId(), secondCard.getId());
    }

    @Test
    @DisplayName("Declining the attack trigger leaves the graveyard and library unchanged")
    void decliningLeavesZonesUnchanged() {
        Card instant = new Shock();
        Card topCard = new LlanowarElves();
        Card cardBelow = new GrizzlyBears();
        gd.playerGraveyards.get(player1.getId()).add(instant);
        harness.setLibrary(player1, List.of(topCard, cardBelow));
        addCreatureReady(player1, new ArdentDustspeaker());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, cardBelow);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Accepting without an instant or sorcery does not exile cards")
    void noMatchingGraveyardCardDoesNotExile() {
        Card creature = new GrizzlyBears();
        Card topCard = new LlanowarElves();
        gd.playerGraveyards.get(player1.getId()).add(creature);
        harness.setLibrary(player1, List.of(topCard));
        addCreatureReady(player1, new ArdentDustspeaker());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty library exiles the instant just put on its bottom")
    void emptyLibraryExilesReturnedInstant() {
        Card instant = new Shock();
        harness.setGraveyard(player1, List.of(instant));
        harness.setLibrary(player1, List.of());
        addCreatureReady(player1, new ArdentDustspeaker());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(instant);
        assertThat(gd.exilePlayPermissions).containsEntry(instant.getId(), player1.getId());
    }

    @Test
    @DisplayName("A one-card library exiles both its original card and the returned spell")
    void oneCardLibraryExilesReturnedSpellSecond() {
        Card sorcery = new Duress();
        Card topCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setLibrary(player1, List.of(topCard));
        addCreatureReady(player1, new ArdentDustspeaker());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard, sorcery);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(topCard.getId(), player1.getId())
                .containsEntry(sorcery.getId(), player1.getId());
    }

    @Test
    @DisplayName("The graveyard card is chosen at resolution rather than when attacking")
    void spellAddedAfterAttackCanBeChosen() {
        Card instant = new Shock();
        Card topCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(topCard));
        addCreatureReady(player1, new ArdentDustspeaker());

        declareAttackers(player1, List.of(0));
        harness.setGraveyard(player1, List.of(instant));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard, instant);
    }

    @Test
    @DisplayName("An exiled instant can be cast for its mana cost after Dustspeaker leaves")
    void exiledSpellCanBeCastAfterSourceLeaves() {
        Card sorcery = new Duress();
        Card instant = new Shock();
        Card secondCard = new GrizzlyBears();
        ArdentDustspeaker dustspeaker = new ArdentDustspeaker();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setLibrary(player1, List.of(instant, secondCard));
        addCreatureReady(player1, dustspeaker);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.setGraveyard(player1, List.of(dustspeaker));
        assertThatThrownBy(() -> harness.castFromExile(player1, instant.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(instant);
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromExile(player1, instant.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(dustspeaker, instant);
    }

    @Test
    @DisplayName("Unplayed cards remain exiled but cannot be played after the turn")
    void unplayedCardsLosePermissionAtCleanup() {
        Card sorcery = new Duress();
        Card instant = new Shock();
        Card secondCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setLibrary(player1, List.of(instant, secondCard));
        addCreatureReady(player1, new ArdentDustspeaker());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));
        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThat(gd.exilePlayPermissions).doesNotContainKeys(instant.getId(), secondCard.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, instant.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(instant, secondCard);
    }
}
