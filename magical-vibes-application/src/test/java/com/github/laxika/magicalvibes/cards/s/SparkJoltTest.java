package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SparkJolt.class, SatyrRambler.class})
class SparkJoltTest extends BaseCardTest {

    @Test
    void dealsDamageThenAllowsScrying() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SparkJolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    void scryReordersTheLibraryAndFinishesResolving() {
        harness.setHand(player1, List.of(new SparkJolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top = deck.get(0);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()).get(0)).isNotSameAs(top);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Spark Jolt");
    }

    @Test
    void mayKeepTheTopCardWithoutChangingLibraryOrder() {
        Card top = new SparkJolt();
        Card bottom = new SatyrRambler();
        harness.setLibrary(player1, List.of(top, bottom));
        harness.setHand(player1, List.of(new SparkJolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertLife(player1, 19);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, bottom);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Spark Jolt");
    }

    @Test
    void resolvesWithAnEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new SparkJolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 19);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Spark Jolt");
    }

    @Test
    void lethalCreatureDamageStillAllowsScrying() {
        harness.addToBattlefield(player2, new SatyrRambler());
        Card top = new SparkJolt();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new SparkJolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Satyr Rambler"));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.assertNotOnBattlefield(player2, "Satyr Rambler");
        harness.assertInGraveyard(player2, "Satyr Rambler");
        harness.assertInGraveyard(player1, "Spark Jolt");
    }

    @Test
    void doesNotScryWhenItsOnlyTargetIsGone() {
        harness.addToBattlefield(player2, new SatyrRambler());
        var targetId = harness.getPermanentId(player2, "Satyr Rambler");
        Card top = new SparkJolt();
        harness.setLibrary(player1, List.of(top));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new SparkJolt()));
        harness.setHand(player2, List.of(new SparkJolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.assertNotOnBattlefield(player2, "Satyr Rambler");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Spark Jolt");
        harness.assertInGraveyard(player2, "Spark Jolt");
    }
}
