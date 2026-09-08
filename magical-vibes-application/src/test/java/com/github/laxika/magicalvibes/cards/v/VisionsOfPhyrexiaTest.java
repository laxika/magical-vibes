package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VisionsOfPhyrexiaTest extends BaseCardTest {

    @Test
    @DisplayName("At your upkeep, it exiles the top card and lets you play it")
    void upkeepExilesTopCardAndLetsYouPlayIt() {
        Card topCard = new Forest();
        harness.addToBattlefield(player1, new VisionsOfPhyrexia());
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, topCard.getId());

        assertThat(findPermanents(player1, "Forest")).hasSize(1);
    }

    @Test
    @DisplayName("Creates a tapped Powerstone at your end step when no card was played from exile")
    void createsPowerstoneWhenNoCardWasPlayedFromExile() {
        harness.addToBattlefield(player1, new VisionsOfPhyrexia());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        List<Permanent> powerstones = findPermanents(player1, "Powerstone");
        assertThat(powerstones).hasSize(1);
        assertThat(powerstones.getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not create a Powerstone when you play the exiled card this turn")
    void doesNotCreatePowerstoneAfterPlayingExiledCard() {
        Card topCard = new Forest();
        harness.addToBattlefield(player1, new VisionsOfPhyrexia());
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, topCard.getId());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
    }

    @Override
    protected void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
