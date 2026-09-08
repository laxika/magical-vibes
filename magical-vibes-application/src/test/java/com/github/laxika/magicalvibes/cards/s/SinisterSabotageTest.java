package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SinisterSabotageTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell and surveils 1")
    void countersSpellAndSurveilsOne() {
        GrizzlyBears bears = new GrizzlyBears();
        Card topCard = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SinisterSabotage()));
        harness.setLibrary(player2, List.of(topCard));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard);
        harness.assertInGraveyard(player2, "Sinister Sabotage");
    }

    @Test
    @DisplayName("Leaves the top card on the library when surveil is declined")
    void declinesSurveil() {
        GrizzlyBears bears = new GrizzlyBears();
        Card topCard = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SinisterSabotage()));
        harness.setLibrary(player2, List.of(topCard));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(topCard);
        harness.assertInGraveyard(player2, "Sinister Sabotage");
    }
}
