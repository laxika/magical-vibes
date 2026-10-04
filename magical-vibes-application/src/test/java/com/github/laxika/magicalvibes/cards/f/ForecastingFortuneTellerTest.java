package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.ErdwalIlluminator;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForecastingFortuneTeller.class})
class ForecastingFortuneTellerTest extends BaseCardTest {

    @Test
    @DisplayName("When Forecasting Fortune Teller enters, one Clue token is created")
    void etbCreatesOneClueToken() {
        harness.castFromHand(player1, new ForecastingFortuneTeller(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> clues = findPermanents(player1, "Clue");
        assertThat(clues).hasSize(1);
        Permanent clue = clues.getFirst();
        assertThat(clue.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(clue.getCard().getSubtypes()).contains(CardSubtype.CLUE);
        assertThat(clue.getCard().isToken()).isTrue();
    }

    @Test
    void clueIsSacrificedAsACostAndDrawsOnlyOnResolution() {
        ForecastingFortuneTeller drawnCard = new ForecastingFortuneTeller();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.castFromHand(player1, new ForecastingFortuneTeller(), "{1}{U}");
        resolveAllTriggers();

        Permanent clue = findPermanent(player1, "Clue");
        clue.setTapped(true);
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, clueIndex, null, null);

        harness.assertNotOnBattlefield(player1, "Clue");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void entryTriggerResolvesAfterFortuneTellerDies() {
        harness.castFromHand(player1, new ForecastingFortuneTeller(), "{1}{U}");
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).isEmpty();

        findPermanent(player1, "Forecasting Fortune Teller").setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Forecasting Fortune Teller");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    void noncastEntryCreatesClueForEnteringCreaturesController() {
        harness.enterBattlefieldAndReturn(player2, new ForecastingFortuneTeller());
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @CardUsed({ErdwalIlluminator.class})
    void creatingClueDoesNotInvestigateOrTriggerIlluminator() {
        harness.addToBattlefield(player1, new ErdwalIlluminator());
        harness.castFromHand(player1, new ForecastingFortuneTeller(), "{1}{U}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.playersWhoInvestigatedThisTurn).doesNotContain(player1.getId());
    }
}
