package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ParameciaColoniex.class, Island.class})
class ParameciaColoniexTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, its controller mills three cards")
    void entersAndMillsThreeCards() {
        Card first = new Island();
        Card second = new Island();
        Card third = new Island();
        Card fourth = new Island();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new ParameciaColoniex()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(first.getId(), second.getId(), third.getId());
    }

    @Test
    @DisplayName("When it dies, accepting the exile puts a targeted creature card on top of its controller's library")
    void deathTriggerExilesItAndReturnsTargetedCreatureToLibrary() {
        ParameciaColoniex coloniex = new ParameciaColoniex();
        Card creature = new ParameciaColoniex();
        Card nonCreature = new Island();
        Card libraryCard = new Island();
        Card opposingCreature = new ParameciaColoniex();
        Permanent permanent = addCreatureReady(player1, coloniex);
        harness.setGraveyard(player1, List.of(creature, nonCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        harness.setLibrary(player1, List.of(libraryCard));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(coloniex);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(coloniex.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(creature.getId(), libraryCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .doesNotContain(creature)
                .doesNotContain(coloniex);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCreature);
    }

    @Test
    @DisplayName("Declining the death-trigger exile leaves both cards in the graveyard")
    void decliningDeathTriggerExileDoesNothing() {
        ParameciaColoniex coloniex = new ParameciaColoniex();
        Card creature = new ParameciaColoniex();
        Permanent permanent = addCreatureReady(player1, coloniex);
        harness.setGraveyard(player1, List.of(creature));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(coloniex.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(coloniex.getId(), creature.getId());
    }

    @Test
    @DisplayName("Entering with fewer than three cards mills the entire remaining library")
    void millsShortLibrary() {
        Card first = new Island();
        Card second = new Island();
        Card opponentCard = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.setLibrary(player2, List.of(opponentCard));

        harness.enterBattlefieldAndReturn(player1, new ParameciaColoniex());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
    }

    @Test
    @DisplayName("The source can be exiled even when no creature remains to target")
    void mayExileWithNoLegalReflexiveTarget() {
        ParameciaColoniex coloniex = new ParameciaColoniex();
        Card land = new Island();
        Permanent permanent = addCreatureReady(player1, coloniex);
        harness.setGraveyard(player1, List.of(land));
        harness.setLibrary(player1, List.of(new Island()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(coloniex);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("No reflexive trigger is created if the source leaves the graveyard before resolution")
    void absentSourceCannotReturnCreature() {
        ParameciaColoniex coloniex = new ParameciaColoniex();
        Card creature = new ParameciaColoniex();
        Card libraryCard = new Island();
        Permanent permanent = addCreatureReady(player1, coloniex);
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(libraryCard));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
        harness.setGraveyard(player1, List.of(creature));
        harness.setExile(player1, List.of(coloniex));
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing the target in response to the reflexive trigger does not undo the source's exile")
    void reflexiveTargetCanBeRemovedInResponse() {
        ParameciaColoniex coloniex = new ParameciaColoniex();
        Card creature = new ParameciaColoniex();
        Card libraryCard = new Island();
        Permanent permanent = addCreatureReady(player1, coloniex);
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(libraryCard));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(coloniex);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(coloniex, creature));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(coloniex, creature);
        assertThat(gd.stack).isEmpty();
    }
}
