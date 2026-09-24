package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VinesoulSpider.class, Forest.class, GrizzlyBears.class})
class VinesoulSpiderTest extends BaseCardTest {

    @Test
    void putsALandFromTheLibraryIntoTheGraveyardAtEndStep() {
        harness.addToBattlefield(player1, new VinesoulSpider());
        Card forest = new Forest();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature, forest));

        advanceToEndStep(player1);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
    }

    @Test
    void doesNothingWhenTheLibraryHasNoLand() {
        harness.addToBattlefield(player1, new VinesoulSpider());
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));

        advanceToEndStep(player1);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
