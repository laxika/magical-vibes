package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StarfighterPilot.class, GrizzlyBears.class})
class StarfighterPilotTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Starfighter Pilot triggers surveil 1")
    void attackingTriggersSurveil() {
        addCreatureReady(player1, new StarfighterPilot());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Declining surveil 1 leaves the top card on the library")
    void decliningSurveilLeavesCardOnTop() {
        addCreatureReady(player1, new StarfighterPilot());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Another creature becoming tapped does not trigger Starfighter Pilot")
    void anotherCreatureBecomingTappedDoesNotTrigger() {
        addCreatureReady(player1, new StarfighterPilot());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
    }
}
