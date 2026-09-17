package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Chlorophant.class, Forest.class})
class ChlorophantTest extends BaseCardTest {

    @Test
    @DisplayName("May put a +1/+1 counter on itself at upkeep")
    void putsCounterAtUpkeep() {
        Permanent chlorophant = addChlorophant();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(chlorophant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Threshold grants a second upkeep counter trigger")
    void thresholdAddsAnotherCounterTrigger() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent chlorophant = addChlorophant();

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(chlorophant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Threshold does not grant a second upkeep counter trigger below seven cards")
    void thresholdIsInactiveBelowSevenCards() {
        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));
        Permanent chlorophant = addChlorophant();

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(chlorophant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining one upkeep counter trigger does not decline the other")
    void decliningOneTriggerDoesNotDeclineTheOther() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent chlorophant = addChlorophant();

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(chlorophant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addChlorophant() {
        return harness.addToBattlefieldAndReturn(player1, new Chlorophant());
    }

    private List<Card> graveyardWithSevenCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            cards.add(new Forest());
        }
        return cards;
    }
}
