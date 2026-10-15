package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WheelOfFate.class, AshcoatBear.class})
class WheelOfFateTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Wheel of Fate with four time counters")
    void suspendExilesWithFourTimeCounters() {
        WheelOfFate card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Suspend can only be activated at sorcery speed")
    void suspendRequiresSorcerySpeed() {
        WheelOfFate card = new WheelOfFate();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Suspend counters are removed only during Wheel of Fate's owner's upkeep")
    void suspendCountersRemainThroughOpponentsUpkeep() {
        WheelOfFate card = suspendCard();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast that redraws seven cards")
    void lastCounterOffersFreeCastAndRedrawsSevenCards() {
        fillLibraries(10);
        WheelOfFate card = suspendCard();
        harness.setHand(player1, List.of(new AshcoatBear(), new AshcoatBear()));
        harness.setHand(player2, List.of(new AshcoatBear()));

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Ashcoat Bear")).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Wheel of Fate");
    }

    @Test
    @DisplayName("Declining the suspend cast leaves Wheel of Fate in exile")
    void decliningSuspendCastLeavesCardInExile() {
        WheelOfFate card = suspendCard();

        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        harness.assertNotInGraveyard(player1, "Wheel of Fate");
    }

    @Test
    @DisplayName("Suspend requires the red mana in its cost")
    void suspendRequiresRedMana() {
        WheelOfFate card = new WheelOfFate();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each owner's upkeep removes exactly one time counter")
    void eachUpkeepRemovesOneTimeCounter() {
        WheelOfFate card = suspendCard();

        for (int remaining = 3; remaining >= 1; remaining--) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();

            assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), remaining);
            assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
            assertThat(gd.interaction.activeInteraction())
                    .isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        }
    }

    @Test
    @DisplayName("Players with empty hands still draw seven cards")
    void emptyHandsStillDrawSevenCards() {
        fillLibraries(10);
        WheelOfFate card = suspendCard();
        harness.setHand(player2, List.of());

        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the suspend cast does not offer it again next upkeep")
    void declinedSuspendCastIsNotOfferedAgain() {
        WheelOfFate card = suspendCard();

        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, false);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertNotInGraveyard(player1, "Wheel of Fate");
    }
    @Test
    @CardUsed({WheelOfFate.class, AshcoatBear.class, PithingNeedle.class})
    @DisplayName("Pithing Needle cannot prevent the suspend special action")
    void pithingNeedleDoesNotPreventSuspend() {
        harness.castFromHand(player1, new PithingNeedle(), "{1}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Wheel of Fate");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);

        WheelOfFate card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).isEmpty();
    }
    private WheelOfFate suspendCard() {
        WheelOfFate card = new WheelOfFate();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }

    private void fillLibraries(int cardsEach) {
        List.of(player1, player2).forEach(player -> {
            List<com.github.laxika.magicalvibes.model.Card> deck = new ArrayList<>();
            for (int i = 0; i < cardsEach; i++) {
                deck.add(new AshcoatBear());
            }
            harness.setLibrary(player, deck);
        });
    }
}
