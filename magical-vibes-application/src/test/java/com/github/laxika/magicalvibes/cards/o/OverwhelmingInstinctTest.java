package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OverwhelmingInstinct.class, ElvishWarrior.class})
class OverwhelmingInstinctTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when three creatures attack")
    void drawsWhenThreeCreaturesAttack() {
        setUpBattlefieldAndLibrary();

        declareAttackers(List.of(1, 2, 3));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when fewer than three creatures attack")
    void doesNotDrawWhenFewerThanThreeCreaturesAttack() {
        setUpBattlefieldAndLibrary();

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws only one card when four creatures attack")
    void drawsOnlyOneCardWhenFourCreaturesAttack() {
        setUpBattlefieldAndLibrary(4, new ElvishWarrior(), new ElvishWarrior());

        declareAttackers(List.of(1, 2, 3, 4));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when the opponent attacks with three creatures")
    void doesNotTriggerForOpponentAttack() {
        setUpBattlefieldAndLibrary();
        harness.setHand(player2, List.of());
        for (int i = 0; i < 3; i++) {
            addCreatureReady(player2, new ElvishWarrior());
        }

        declareAttackers(player2, List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each copy draws a card for the same attack")
    void eachCopyTriggersIndependently() {
        setUpBattlefieldAndLibrary(3, new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior());
        harness.addToBattlefield(player1, new OverwhelmingInstinct());

        declareAttackers(List.of(1, 2, 3));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can draw again in another combat in the same turn")
    void triggersAgainInAnotherCombat() {
        setUpBattlefieldAndLibrary(3, new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior());

        declareAttackers(List.of(1, 2, 3));
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).forEach(permanent -> {
            permanent.setTapped(false);
            permanent.setAttacking(false);
        });
        declareAttackers(List.of(1, 2, 3));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void setUpBattlefieldAndLibrary() {
        setUpBattlefieldAndLibrary(3, new ElvishWarrior());
    }

    private void setUpBattlefieldAndLibrary(int creatureCount, Card... libraryCards) {
        harness.addToBattlefield(player1, new OverwhelmingInstinct());
        for (int i = 0; i < creatureCount; i++) {
            addCreatureReady(player1, new ElvishWarrior());
        }
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(libraryCards));
    }
}
