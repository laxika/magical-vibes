package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SurvivorOfTheUnseenTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards, then puts a chosen hand card on top of the library")
    void drawsTwoAndPutsChosenCardOnTop() {
        Permanent survivor = addCreatureReady(player1, new SurvivorOfTheUnseen());
        Card first = new Shock();
        Card second = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(survivor.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Survivor of the Unseen")
    void paysCumulativeUpkeep() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new SurvivorOfTheUnseen());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(survivor.getCounterCount(CounterType.AGE)).isEqualTo(1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(survivor);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Survivor of the Unseen")
    void declineSacrifices() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new SurvivorOfTheUnseen());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(survivor);
        harness.assertInGraveyard(player1, "Survivor of the Unseen");
    }
}
