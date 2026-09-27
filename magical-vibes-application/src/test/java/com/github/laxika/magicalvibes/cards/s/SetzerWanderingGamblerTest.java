package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TheGoldSaucer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SetzerWanderingGambler.class, GrizzlyBears.class, TheGoldSaucer.class})
class SetzerWanderingGamblerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates The Blackjack and it can be crewed")
    void enteringCreatesBlackjackThatCanBeCrewed() {
        castSetzer();
        Permanent blackjack = findPermanent(player1, "The Blackjack");
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(blackjack), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, blackjack)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A Vehicle dealing combat damage flips a coin and a win creates two tapped Treasures")
    void vehicleCombatDamageAndCoinFlipWinCreateTreasures() {
        castSetzer();
        Permanent blackjack = findPermanent(player1, "The Blackjack");
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(blackjack), null, null);
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
        harness.passBothPriorities();

        boolean won = gameLogContains("wins the coin flip");
        boolean lost = gameLogContains("loses the coin flip");
        assertThat(won != lost).isTrue();
        assertThat(findPermanents(player1, "Treasure")).hasSize(won ? 2 : 0);
        if (won) {
            assertThat(findPermanents(player1, "Treasure")).allSatisfy(treasure -> assertThat(treasure.isTapped()).isTrue());
        }
    }

    private void castSetzer() {
        harness.setHand(player1, List.of(new SetzerWanderingGambler()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
