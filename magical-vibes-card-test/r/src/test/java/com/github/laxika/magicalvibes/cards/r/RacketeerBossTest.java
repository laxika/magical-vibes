package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RacketeerBoss.class, GrizzlyBears.class, Shock.class, Counterspell.class})
class RacketeerBossTest extends BaseCardTest {

    @Test
    void choosesUpToTwoCreatureCardsAndEachCreatesOnlyOneTreasure() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        Shock nonCreature = new Shock();
        harness.setHand(player1, List.of(new RacketeerBoss(), first, second, nonCreature));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId());

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        assertThat(gd.perpetualAbilityGrants).containsKeys(first.getId(), second.getId());

        castBears(first);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.perpetualAbilityGrants).containsKey(second.getId());

        castBears(second);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(gd.perpetualAbilityGrants).doesNotContainKeys(first.getId(), second.getId());
    }

    @Test
    @DisplayName("A spell countered before its cast trigger resolves keeps the ability on the card in the graveyard")
    void counteredSpellKeepsTheAbilityOnTheNewObject() {
        GrizzlyBears chosen = new GrizzlyBears();
        harness.setHand(player1, List.of(new RacketeerBoss(), chosen));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, chosen.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.perpetualAbilityGrants).containsKey(chosen.getId());
    }

    private void castBears(GrizzlyBears bears) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, gd.playerHands.get(player1.getId()).indexOf(bears));
        resolveAllTriggers();
    }

    @Test
    @DisplayName("The log doesn't name the card chosen from the hand")
    void logDoesNotNameTheChosenHandCard() {
        GrizzlyBears chosen = new GrizzlyBears();
        harness.setHand(player1, List.of(new RacketeerBoss(), chosen));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.gameLog).noneMatch(line -> line.plainText().contains("Grizzly Bears"));
        assertThat(gd.gameLog).anyMatch(line -> line.plainText().contains("perpetually gains"));
    }

    @Test
    @DisplayName("Choosing two hand cards logs one choice line and one grant line, neither counting the cards")
    void choiceOfTwoHandCardsCountsNothingInTheLog() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setHand(player1, List.of(new RacketeerBoss(), first, second));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.gameLog).filteredOn(line -> line.plainText().contains("chosen")
                        || line.plainText().contains("chooses") || line.plainText().contains("perpetually"))
                .extracting(line -> line.plainText())
                .containsExactly(
                        "Cards in " + player1.getUsername() + "'s hand are chosen.",
                        "Each affected card in " + player1.getUsername() + "'s hand perpetually gains an ability.");
    }
}
