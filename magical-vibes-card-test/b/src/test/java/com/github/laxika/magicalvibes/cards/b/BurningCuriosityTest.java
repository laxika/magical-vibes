package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BurningCuriosity.class, GrizzlyBears.class, RagingGoblin.class})
class BurningCuriosityTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private List<Card> putTopCards(int count) {
        List<Card> cards = java.util.stream.IntStream.range(0, count)
                .<Card>mapToObj(i -> new RagingGoblin())
                .toList();
        gd.playerDecks.get(player1.getId()).addAll(0, cards);
        return cards;
    }

    @Test
    void withoutBlightExilesTopTwoCards() {
        List<Card> topCards = putTopCards(3);
        harness.setHand(player1, List.of(new BurningCuriosity()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(topCards.get(0).getId(), topCards.get(1).getId());
        assertThat(gd.playerDecks.get(player1.getId())).contains(topCards.get(2));
        assertThat(gd.exilePlayPermissions)
                .containsKeys(topCards.get(0).getId(), topCards.get(1).getId())
                .doesNotContainKey(topCards.get(2).getId());
        assertThat(gd.exilePlayPermissionsAwaitNextTurnOfPlayer)
                .containsEntry(topCards.get(0).getId(), player1.getId());
    }

    @Test
    void blightExilesTopThreeCards() {
        List<Card> topCards = putTopCards(4);
        Permanent blightCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BurningCuriosity()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, blightCreature.getId());
        assertThat(blightCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(blightCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(topCards.get(0).getId(), topCards.get(1).getId(), topCards.get(2).getId());
        assertThat(gd.playerDecks.get(player1.getId())).contains(topCards.get(3));
    }

    @Test
    void canDeclineBlightWithCreatureAvailable() {
        List<Card> topCards = putTopCards(3);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BurningCuriosity()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(topCards.get(0), topCards.get(1));
    }

    @Test
    void blightStillExilesThreeWhenCreatureDiesFromCost() {
        List<Card> topCards = putTopCards(4);
        Permanent creature = addCreatureReady(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new BurningCuriosity()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Raging Goblin");
        harness.assertNotOnBattlefield(player1, "Raging Goblin");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(topCards.get(0), topCards.get(1), topCards.get(2));
    }

    @Test
    void shortLibraryExilesOnlyAvailableCards() {
        Card card = new RagingGoblin();
        harness.setLibrary(player1, List.of(card));
        harness.setHand(player1, List.of(new BurningCuriosity()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).containsEntry(card.getId(), player1.getId());
    }

    @Test
    void canCastExiledCreatureByPayingItsManaCost() {
        List<Card> topCards = putTopCards(2);
        harness.setHand(player1, List.of(new BurningCuriosity()));
        addMana();
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, topCards.get(0).getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Raging Goblin");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCards.get(1))
                .doesNotContain(topCards.get(0));
    }

    @Test
    void permissionLastsThroughNextTurnThenExpiresWithoutReturningCards() {
        List<Card> topCards = putTopCards(4);
        harness.setHand(player1, List.of(new BurningCuriosity()));
        addMana();
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(topCards.get(0).getId(), player1.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(topCards.get(0).getId(), player1.getId());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions)
                .doesNotContainKeys(topCards.get(0).getId(), topCards.get(1).getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(topCards.get(0), topCards.get(1));
    }
}
