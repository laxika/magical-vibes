package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YawgmothsTestament.class, DarkRitual.class, Forest.class})
class YawgmothsTestamentTest extends BaseCardTest {

    @Test
    void playsFaceUpCardsFromExile() {
        YawgmothsTestament testament = new YawgmothsTestament();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(testament));
        harness.setExile(player1, List.of(forest));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
        gs.playCardFromExile(gd, player1, forest.getId(), null, null);

        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void putsCardsOnBottomOfLibraryInsteadOfGraveyardOrExile() {
        YawgmothsTestament testament = new YawgmothsTestament();
        DarkRitual ritual = new DarkRitual();
        Forest libraryCard = new Forest();
        harness.setHand(player1, List.of(testament));
        harness.setExile(player1, List.of(ritual));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
        harness.castFromExile(player1, ritual.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard, testament, ritual);
    }
}
