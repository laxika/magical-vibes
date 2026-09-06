package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RummagingWizard.class)
class RummagingWizardTest extends BaseCardTest {

    @Test
    @DisplayName("Pays {2}{U} to surveil one")
    void paysManaToSurveilOne() {
        GameData gameData = harness.getGameData();
        Card topCard = new RummagingWizard();
        harness.setLibrary(player1, List.of(topCard));
        addCreatureReady(player1, new RummagingWizard());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gameData.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gameData.playerGraveyards.get(player1.getId())).contains(topCard);
    }
}
