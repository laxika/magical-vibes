package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BlackMarketConnections.class)
class BlackMarketConnectionsTest extends BaseCardTest {

    private static final String SELL_CONTRABAND =
            "Sell Contraband — Create a Treasure token. You lose 1 life.";
    private static final String BUY_INFORMATION =
            "Buy Information — Draw a card. You lose 2 life.";
    private static final String HIRE_A_MERCENARY =
            "Hire a Mercenary — Create a 3/2 colorless Shapeshifter creature token with changeling. You lose 3 life.";

    @Test
    @DisplayName("Sell Contraband creates a Treasure and costs 1 life")
    void sellContraband() {
        harness.addToBattlefield(player1, new BlackMarketConnections());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        resolveModes(player1, SELL_CONTRABAND);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        harness.assertOnBattlefield(player1, "Treasure");
    }

    @Test
    @DisplayName("Buy Information draws a card and costs 2 life")
    void buyInformation() {
        harness.setLibrary(player1, List.of(new BlackMarketConnections()));
        harness.addToBattlefield(player1, new BlackMarketConnections());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        resolveModes(player1, BUY_INFORMATION);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Hire a Mercenary creates a 3/2 colorless Shapeshifter with changeling and costs 3 life")
    void hireAMercenary() {
        harness.addToBattlefield(player1, new BlackMarketConnections());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        resolveModes(player1, HIRE_A_MERCENARY);

        Permanent token = findPermanent(player1, "Shapeshifter");
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SHAPESHIFTER);
        assertThat(token.getCard().getKeywords()).contains(Keyword.CHANGELING);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("The controller may choose all three modes")
    void choosesAllModes() {
        harness.setLibrary(player1, List.of(new BlackMarketConnections()));
        harness.addToBattlefield(player1, new BlackMarketConnections());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToPrecombatMain(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, SELL_CONTRABAND);
        harness.handleListChoice(player1, BUY_INFORMATION);
        harness.handleListChoice(player1, HIRE_A_MERCENARY);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 6);
        harness.assertOnBattlefield(player1, "Treasure");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Shapeshifter"))
                .hasSize(1);
    }

    private void resolveModes(Player player, String... modes) {
        advanceToPrecombatMain(player);
        for (String mode : modes) {
            harness.handleListChoice(player, mode);
        }
        harness.handleListChoice(player, ChooseOneEffect.FINISH_MODE_SELECTION);
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();
    }
}
