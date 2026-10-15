package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EonHub;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.ExileToOwnerGraveyardAtNextUpkeep;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThreeWishes.class, Island.class, Shock.class, GrizzlyBears.class, EonHub.class})
class ThreeWishesTest extends BaseCardTest {

    private void setLibraryTop(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }

    private void castThreeWishes() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ThreeWishes(), "{1}{U}{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Exiles top three face down with play permission and schedules upkeep graveyard cleanup")
    void exilesTopThreeFaceDownWithPlayPermission() {
        Card a = new Island();
        Card b = new Shock();
        Card c = new GrizzlyBears();
        Card leftover = new Island();
        setLibraryTop(a, b, c, leftover);

        castThreeWishes();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(a.getId(), b.getId(), c.getId());
        assertThat(gd.findExiledCard(a.getId()).faceDown()).isTrue();
        assertThat(gd.findExiledCard(b.getId()).faceDown()).isTrue();
        assertThat(gd.findExiledCard(c.getId()).faceDown()).isTrue();
        assertThat(gd.exilePlayPermissions.get(a.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissions.get(b.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissions.get(c.getId())).isEqualTo(player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(leftover);

        List<ExileToOwnerGraveyardAtNextUpkeep> scheduled =
                gd.getDelayedActions(ExileToOwnerGraveyardAtNextUpkeep.class);
        assertThat(scheduled).hasSize(3);
        assertThat(scheduled).allMatch(s -> s.controllerId().equals(player1.getId()));
        assertThat(scheduled).allMatch(s -> s.ownerId().equals(player1.getId()));
        assertThat(scheduled).extracting(ExileToOwnerGraveyardAtNextUpkeep::cardId)
                .containsExactlyInAnyOrder(a.getId(), b.getId(), c.getId());
    }

    @Test
    @DisplayName("Unplayed exiled cards go to the graveyard at the caster's next upkeep")
    void unplayedCardsGoToGraveyardAtNextUpkeep() {
        Card a = new Island();
        Card b = new Shock();
        Card c = new GrizzlyBears();
        setLibraryTop(a, b, c);

        castThreeWishes();

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> List.of(a.getId(), b.getId(), c.getId()).contains(card.getId()));
        assertThat(gd.exilePlayPermissions)
                .doesNotContainKeys(a.getId(), b.getId(), c.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(a.getId(), b.getId(), c.getId());
        assertThat(gd.getDelayedActions(ExileToOwnerGraveyardAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Cleanup does not fire on an opponent's upkeep")
    void cleanupDoesNotFireOnOpponentUpkeep() {
        Card a = new Island();
        Card b = new Shock();
        Card c = new GrizzlyBears();
        setLibraryTop(a, b, c);

        castThreeWishes();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(a.getId(), b.getId(), c.getId());
        assertThat(gd.exilePlayPermissions.get(a.getId())).isEqualTo(player1.getId());
        assertThat(gd.getDelayedActions(ExileToOwnerGraveyardAtNextUpkeep.class)).hasSize(3);
    }

    @Test
    @DisplayName("A played card is not put into the graveyard at upkeep")
    void playedCardIsNotPutIntoGraveyard() {
        Card land = new Island();
        Card spell = new Shock();
        Card creature = new GrizzlyBears();
        setLibraryTop(land, spell, creature);

        castThreeWishes();

        harness.castFromExile(player1, land.getId());
        harness.assertOnBattlefield(player1, "Island");

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(spell.getId(), creature.getId())
                .doesNotContain(land.getId());
    }

    @Test
    @DisplayName("A nonland card can be cast from exile before the next turn")
    void nonlandCardCanBeCastFromExile() {
        Card land = new Island();
        Card spell = new Shock();
        Card creature = new GrizzlyBears();
        setLibraryTop(land, spell, creature);

        castThreeWishes();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, spell.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(spell.getId()));
        assertThat(gd.exilePlayPermissions).doesNotContainKey(spell.getId());
    }

    @Test
    @DisplayName("Exiles fewer cards when the library is short")
    void shortLibraryExilesWhatIsAvailable() {
        Card only = new Island();
        setLibraryTop(only);

        castThreeWishes();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(only.getId());
        assertThat(gd.getDelayedActions(ExileToOwnerGraveyardAtNextUpkeep.class)).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty library produces no exile entries or cleanup actions")
    void emptyLibraryProducesNoExileEntries() {
        harness.setLibrary(player1, List.of());

        castThreeWishes();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getDelayedActions(ExileToOwnerGraveyardAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Unplayed cards remain exiled until the single upkeep trigger resolves")
    void cleanupUsesTheStack() {
        Card a = new Island();
        Card b = new Shock();
        Card c = new GrizzlyBears();
        setLibraryTop(a, b, c, new Island());
        castThreeWishes();

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(a, b, c);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(a, b, c);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, b.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(a, b, c);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(a, b, c);
    }

    @Test
    @DisplayName("Play permission expires at the next turn even when upkeep is skipped")
    void skippedUpkeepDoesNotExtendPlayPermission() {
        Card spell = new Shock();
        setLibraryTop(spell, new Island(), new GrizzlyBears(), new Island(), new Island());
        harness.addToBattlefield(player1, new EonHub());
        castThreeWishes();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature can be cast from exile by paying its normal mana cost")
    void creatureCanBeCastFromExile() {
        Card creature = new GrizzlyBears();
        setLibraryTop(creature, new Island(), new Shock());
        castThreeWishes();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("The opponent cannot cast the face-down exiled cards")
    void opponentCannotUsePlayPermission() {
        Card spell = new Shock();
        setLibraryTop(spell, new Island(), new GrizzlyBears());
        castThreeWishes();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player2, spell.getId(), player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("The caster can look at all the face-down cards even without mana to cast them")
    void casterCanLookAtExiledCards() throws Exception {
        Card a = new Shock();
        Card b = new GrizzlyBears();
        Card c = new GrizzlyBears();
        setLibraryTop(a, b, c);
        castThreeWishes();
        harness.publishState();

        var mapper = new JacksonConfig().objectMapper();
        GameStateMessage casterState = mapper.readValue(harness.getConn1()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);
        GameStateMessage opponentState = mapper.readValue(harness.getConn2()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);

        assertThat(casterState.lookedAtExileCards()).extracting(card -> card.id())
                .contains(a.getId(), b.getId(), c.getId());
        assertThat(opponentState.lookedAtExileCards()).extracting(card -> card.id())
                .doesNotContain(a.getId(), b.getId(), c.getId());
    }
}
