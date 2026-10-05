package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(LukeCageHeroForHire.class)
class LukeCageHeroForHireTest extends BaseCardTest {

    @Test
    void createsTreasureAtBeginningOfCombatOnYourTurn() {
        harness.addToBattlefield(player1, new LukeCageHeroForHire());

        advanceToBeginningOfCombat(player1);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void doesNotCreateTreasureAtBeginningOfCombatOnOpponentsTurn() {
        harness.addToBattlefield(player1, new LukeCageHeroForHire());

        advanceToBeginningOfCombat(player2);

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
    }

    @Test
    void triggerResolvesAfterLukeLeavesTheBattlefield() {
        var luke = harness.addToBattlefieldAndReturn(player1, new LukeCageHeroForHire());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        gd.playerBattlefields.get(player1.getId()).remove(luke);
        gd.playerGraveyards.get(player1.getId()).add(luke.getCard());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void treasureCanBeSacrificedImmediatelyForColoredMana() {
        harness.addToBattlefield(player1, new LukeCageHeroForHire());
        advanceToBeginningOfCombat(player1);
        var treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.isTapped()).isFalse();

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(treasure), null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
