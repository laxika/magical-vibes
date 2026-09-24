package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlackMarketConnections.class, Forest.class})
class BlackMarketConnectionsTest extends BaseCardTest {

    private static final String SELL_CONTRABAND =
            "Sell Contraband â€” Create a Treasure token. You lose 1 life.";
    private static final String BUY_INFORMATION =
            "Buy Information â€” Draw a card. You lose 2 life.";
    private static final String HIRE_MERCENARY =
            "Hire a Mercenary â€” Create a 3/2 colorless Shapeshifter creature token with changeling. You lose 3 life.";

    @Test
    void sellsContrabandForATreasureAndOneLife() {
        addConnections();

        advanceToPrecombatMain(player1);
        chooseModes(SELL_CONTRABAND);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    void buysInformationForACardAndTwoLife() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        addConnections();

        advanceToPrecombatMain(player1);
        int handSize = gd.playerHands.get(player1.getId()).size();
        chooseModes(BUY_INFORMATION);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void hiresMercenaryAsAThreeTwoColorlessChangelingAndLosesThreeLife() {
        addConnections();

        advanceToPrecombatMain(player1);
        chooseModes(HIRE_MERCENARY);

        Permanent token = findPermanent(player1, "Shapeshifter");
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SHAPESHIFTER);
        assertThat(token.getCard().getKeywords()).containsExactly(Keyword.CHANGELING);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    void mayChooseAllThreeModes() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        addConnections();

        advanceToPrecombatMain(player1);
        int handSize = gd.playerHands.get(player1.getId()).size();
        chooseModes(SELL_CONTRABAND, BUY_INFORMATION, HIRE_MERCENARY);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Shapeshifter")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
    }

    @Test
    void triggersOnlyOnTheControllersFirstMainPhase() {
        addConnections();

        advanceToPrecombatMain(player2);

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addConnections() {
        return harness.addToBattlefieldAndReturn(player1, new BlackMarketConnections());
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void chooseModes(String... modes) {
        for (String mode : modes) {
            harness.handleListChoice(player1, mode);
        }
        harness.handleListChoice(player1, "Done");
        harness.passBothPriorities();
    }
}
