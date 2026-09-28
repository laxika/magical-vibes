package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AmyPond;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoryWilliams.class, AmyPond.class})
class RoryWilliamsTest extends BaseCardTest {

    @Test
    void partnerWithAmyLetsTargetPlayerSearchTheirLibrary() {
        AmyPond amy = new AmyPond();
        harness.setLibrary(player2, List.of(amy));
        harness.setHand(player1, List.of(new RoryWilliams()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).contains(amy);
    }

    @Test
    void castingFromHandExilesRoryWithSuspendAndInvestigates() {
        RoryWilliams rory = new RoryWilliams();
        harness.setHand(player1, List.of(rory));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(rory);
        assertThat(gd.exiledCardTimeCounters).containsEntry(rory.getId(), 3);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castingFromExileDoesNotExileAgainOrInvestigate() {
        RoryWilliams rory = new RoryWilliams();
        harness.setExile(player1, List.of(rory));
        harness.castFromExile(player1, rory.getId(), player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanent(player1, "Rory Williams")).isNotNull();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(rory.getId());
    }
}
