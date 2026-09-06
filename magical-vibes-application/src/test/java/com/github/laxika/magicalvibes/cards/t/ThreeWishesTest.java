package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.ExileToOwnerGraveyardAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThreeWishes.class, Island.class, Shock.class, GrizzlyBears.class})
class ThreeWishesTest extends BaseCardTest {

    private StepTriggerService stepTriggerService() {
        return GameTestEngineContext.get().getBean(StepTriggerService.class);
    }

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

        gd.activePlayerId = player1.getId();
        harness.inMutationScope(() -> stepTriggerService().handleUpkeepTriggers(gd));

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

        gd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService().handleUpkeepTriggers(gd));

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

        gd.activePlayerId = player1.getId();
        harness.inMutationScope(() -> stepTriggerService().handleUpkeepTriggers(gd));

        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(spell.getId(), creature.getId())
                .doesNotContain(land.getId());
    }

    @Test
    @DisplayName("A nonland card can be cast from exile before the next upkeep")
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
}
