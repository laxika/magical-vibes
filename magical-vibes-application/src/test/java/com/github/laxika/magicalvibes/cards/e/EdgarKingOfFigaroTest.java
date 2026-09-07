package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.ChanceEncounter;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RalZarek;
import com.github.laxika.magicalvibes.cards.s.SorcerersStrongbox;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EdgarKingOfFigaro.class, ChanceEncounter.class, Forest.class, RalZarek.class,
        SorcerersStrongbox.class})
class EdgarKingOfFigaroTest extends BaseCardTest {

    @Test
    void entersAndDrawsForEachArtifactYouControl() {
        harness.addToBattlefield(player1, new SorcerersStrongbox());
        harness.addToBattlefield(player1, new SorcerersStrongbox());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new SorcerersStrongbox());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new EdgarKingOfFigaro(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        int handBeforeTrigger = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeTrigger + 2);
    }

    @Test
    void firstCoinFlipEachTurnComesUpHeadsAndIsWon() {
        harness.addToBattlefield(player1, new EdgarKingOfFigaro());
        harness.addToBattlefield(player1, new SorcerersStrongbox());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Sorcerer's Strongbox"));
        assertThat(gameLogContains("wins the coin flip for Sorcerer's Strongbox")).isTrue();
    }

    @Test
    void allCoinsInFirstMultiCoinFlipAreHeadsAndWins() {
        harness.addToBattlefield(player1, new EdgarKingOfFigaro());
        Permanent ral = new Permanent(new RalZarek());
        ral.setCounterCount(CounterType.LOYALTY, 7);
        ral.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ral);
        Permanent chanceEncounter = harness.addToBattlefieldAndReturn(player1, new ChanceEncounter());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 1, 2, null, null);
        resolveAllTriggers();

        assertThat(chanceEncounter.getCounterCount(CounterType.LUCK)).isEqualTo(5);
        assertThat(gd.extraTurns).hasSize(5);
    }
}
