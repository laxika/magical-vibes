package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({ParameciaColoniex.class, GrizzlyBears.class, Island.class})
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
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(first.getId(), second.getId(), third.getId());
    }

    @Test
    @DisplayName("When it dies, accepting the exile puts a targeted creature card on top of its controller's library")
    void deathTriggerExilesItAndReturnsTargetedCreatureToLibrary() {
        ParameciaColoniex coloniex = new ParameciaColoniex();
        Card creature = new GrizzlyBears();
        Card nonCreature = new Island();
        Card libraryCard = new Island();
        Permanent permanent = addCreatureReady(player1, coloniex);
        harness.setGraveyard(player1, List.of(creature, nonCreature));
        harness.setLibrary(player1, List.of(libraryCard));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(creature.getId());
        assertThat(choice.validCardIds()).doesNotContain(nonCreature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(coloniex.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(creature.getId(), libraryCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .doesNotContain(creature)
                .doesNotContain(coloniex);
    }

    @Test
    @DisplayName("Declining the death-trigger exile leaves both cards in the graveyard")
    void decliningDeathTriggerExileDoesNothing() {
        ParameciaColoniex coloniex = new ParameciaColoniex();
        Card creature = new GrizzlyBears();
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
}
