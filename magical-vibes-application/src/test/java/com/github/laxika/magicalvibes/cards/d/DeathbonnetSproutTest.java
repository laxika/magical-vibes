package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathbonnetSprout.class, DeathbonnetHulk.class, GrizzlyBears.class, Shock.class})
class DeathbonnetSproutTest extends BaseCardTest {

    @Test
    @DisplayName("Mills a card and transforms when the third creature card reaches the graveyard")
    void millsAndTransformsAtThreeCreatureCards() {
        Permanent sprout = addCreatureReady(player1, new DeathbonnetSprout());
        Card milledCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(milledCreature));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milledCreature);
        assertThat(sprout.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not transform when the milled card is not a creature")
    void doesNotTransformWithoutThreeCreatureCards() {
        Permanent sprout = addCreatureReady(player1, new DeathbonnetSprout());
        Card milledNoncreature = new Shock();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(milledNoncreature));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milledNoncreature);
        assertThat(sprout.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("May exile a creature card from any graveyard and put a counter on itself")
    void exilesCreatureFromAnyGraveyardAndPutsCounterOnSource() {
        Permanent hulk = addCreatureReady(player1, new DeathbonnetHulk());
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentCreature));

        chooseGraveyardCard(opponentCreature);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentCreature);
        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not put a counter on itself when a noncreature card is exiled")
    void doesNotPutCounterForNoncreature() {
        Permanent hulk = addCreatureReady(player1, new DeathbonnetHulk());
        Card opponentNoncreature = new Shock();
        harness.setGraveyard(player2, List.of(opponentNoncreature));

        chooseGraveyardCard(opponentNoncreature);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentNoncreature);
        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("May decline to exile a card")
    void mayDeclineExile() {
        Permanent hulk = addCreatureReady(player1, new DeathbonnetHulk());
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentCreature));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();

        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void chooseGraveyardCard(Card card) {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        resolveAllTriggers();
    }
}
