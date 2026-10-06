package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EdgarKingOfFigaro;
import com.github.laxika.magicalvibes.cards.t.TheGoldSaucer;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SetzerWanderingGambler.class, EdgarKingOfFigaro.class, TheGoldSaucer.class, TurnToFrog.class})
class SetzerWanderingGamblerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates The Blackjack and it can be crewed")
    void enteringCreatesBlackjackThatCanBeCrewed() {
        castSetzer();
        Permanent blackjack = findPermanent(player1, "The Blackjack");
        Permanent crew = findPermanent(player1, "Setzer, Wandering Gambler");

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(blackjack), null, null);
        harness.handlePermanentChosen(player1, crew.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, blackjack)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A Vehicle dealing combat damage flips a coin and a win creates two tapped Treasures")
    void vehicleCombatDamageAndCoinFlipWinCreateTreasures() {
        castSetzer();
        Permanent blackjack = findPermanent(player1, "The Blackjack");
        Permanent crew = findPermanent(player1, "Setzer, Wandering Gambler");

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(blackjack), null, null);
        harness.handlePermanentChosen(player1, crew.getId());
        harness.passBothPriorities();

        blackjack.setSummoningSick(false);
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(blackjack)));
        resolveCombat(player1);
        resolveAllTriggers();

        boolean won = gameLogContains("wins the coin flip");
        boolean lost = gameLogContains("loses the coin flip");
        assertThat(won != lost).isTrue();
        assertThat(findPermanents(player1, "Treasure")).hasSize(won ? 2 : 0);
        if (won) {
            assertThat(findPermanents(player1, "Treasure")).allSatisfy(treasure -> assertThat(treasure.isTapped()).isTrue());
        }
    }

    @Test
    @DisplayName("Winning any coin flip creates two tapped Treasures")
    void winningAnyCoinFlipCreatesTreasures() {
        harness.addToBattlefield(player1, new SetzerWanderingGambler());
        Permanent saucer = harness.addToBattlefieldAndReturn(player1, new TheGoldSaucer());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(saucer), 1, null, null);
        resolveAllTriggers();

        boolean won = gameLogContains("wins the coin flip");
        boolean lost = gameLogContains("loses the coin flip");
        assertThat(won != lost).isTrue();
        assertThat(findPermanents(player1, "Treasure")).hasSize(won ? 3 : 0);
        if (won) {
            assertThat(findPermanents(player1, "Treasure")).filteredOn(Permanent::isTapped).hasSize(2);
        }
    }

    @Test
    @DisplayName("A guaranteed coin-flip win creates two tapped Treasures")
    void guaranteedWinCreatesTwoTappedTreasures() {
        harness.addToBattlefield(player1, new SetzerWanderingGambler());
        harness.addToBattlefield(player1, new EdgarKingOfFigaro());
        Permanent saucer = harness.addToBattlefieldAndReturn(player1, new TheGoldSaucer());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(saucer), 1, null, null);
        resolveAllTriggers();

        assertThat(gameLogContains("wins the coin flip")).isTrue();
        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
        assertThat(findPermanents(player1, "Treasure")).filteredOn(Permanent::isTapped).hasSize(2);
    }

    @Test
    @DisplayName("Setzer does not trigger for coin-flip wins while it has lost all abilities")
    void losingAbilitiesPreventsCoinWinTrigger() {
        Permanent setzer = harness.addToBattlefieldAndReturn(player1, new SetzerWanderingGambler());
        harness.addToBattlefield(player1, new EdgarKingOfFigaro());
        Permanent saucer = harness.addToBattlefieldAndReturn(player1, new TheGoldSaucer());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, setzer.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(saucer), 1, null, null);
        resolveAllTriggers();

        assertThat(gameLogContains("wins the coin flip")).isTrue();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).allSatisfy(treasure -> assertThat(treasure.isTapped()).isFalse());
    }

    @Test
    @DisplayName("A non-Vehicle dealing combat damage does not flip a coin")
    void nonVehicleCombatDamageDoesNotFlipCoin() {
        Permanent setzer = addCreatureReady(player1, new SetzerWanderingGambler());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(setzer)));
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gameLogContains("coin flip")).isFalse();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Entering creates exactly one legendary colorless flying Vehicle")
    void enteringCreatesOracleBlackjackToken() {
        castSetzer();

        assertThat(findPermanents(player1, "The Blackjack")).hasSize(1);
        Permanent blackjack = findPermanent(player1, "The Blackjack");
        assertThat(blackjack.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(blackjack.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(blackjack.getCard().getColors()).isEmpty();
        assertThat(gqs.isCreature(gd, blackjack)).isFalse();
        assertThat(gqs.hasKeyword(gd, blackjack, Keyword.FLYING)).isTrue();
        assertThat(blackjack.getCard().getPower()).isEqualTo(3);
        assertThat(blackjack.getCard().getToughness()).isEqualTo(3);
        assertThat(blackjack.isTapped()).isFalse();
        assertThat(findPermanents(player2, "The Blackjack")).isEmpty();
    }

    @Test
    @DisplayName("An opponent's coin-flip win does not create Treasures for Setzer's controller")
    void opponentsCoinWinDoesNotTriggerSetzer() {
        harness.addToBattlefield(player1, new SetzerWanderingGambler());
        harness.addToBattlefield(player2, new EdgarKingOfFigaro());
        Permanent saucer = harness.addToBattlefieldAndReturn(player2, new TheGoldSaucer());
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(saucer), 1, null, null);
        resolveAllTriggers();

        assertThat(gameLogContains("wins the coin flip")).isTrue();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
    }

    private void castSetzer() {
        harness.setHand(player1, List.of(new SetzerWanderingGambler()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
