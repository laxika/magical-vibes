package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TwoHeadedHellkite.class})
class TwoHeadedHellkiteTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking draws two cards")
    void attackingDrawsTwoCards() {
        addCreatureReady(player1, new TwoHeadedHellkite());
        harness.setLibrary(player1, List.of(new TwoHeadedHellkite(), new TwoHeadedHellkite()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Haste allows a newly entered Hellkite to attack and draw")
    void newlyEnteredHellkiteCanAttack() {
        harness.addToBattlefield(player1, new TwoHeadedHellkite());
        harness.setLibrary(player1, List.of(new TwoHeadedHellkite(), new TwoHeadedHellkite()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Each attacking Hellkite draws two cards independently")
    void eachAttackingHellkiteDrawsTwoCards() {
        addCreatureReady(player1, new TwoHeadedHellkite());
        addCreatureReady(player1, new TwoHeadedHellkite());
        harness.setLibrary(player1, List.of(new TwoHeadedHellkite(), new TwoHeadedHellkite(),
                new TwoHeadedHellkite(), new TwoHeadedHellkite()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 4);
    }

    @Test
    @DisplayName("An opponent's attacking Hellkite draws for its controller")
    void opponentDrawsForTheirAttackingHellkite() {
        addCreatureReady(player2, new TwoHeadedHellkite());
        harness.setLibrary(player2, List.of(new TwoHeadedHellkite(), new TwoHeadedHellkite()));
        int controllerHandBefore = gd.playerHands.get(player2.getId()).size();
        int defenderHandBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(controllerHandBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(defenderHandBefore);
    }

    @Test
    @DisplayName("The attack trigger still draws after Hellkite leaves the battlefield")
    void attackTriggerSurvivesSourceLeavingBattlefield() {
        Permanent hellkite = addCreatureReady(player1, new TwoHeadedHellkite());
        harness.setLibrary(player1, List.of(new TwoHeadedHellkite(), new TwoHeadedHellkite()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        gd.playerBattlefields.get(player1.getId()).remove(hellkite);
        gd.playerGraveyards.get(player1.getId()).add(hellkite.getCard());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("A Hellkite that does not attack does not draw cards")
    void nonattackingHellkiteDoesNotDrawCards() {
        addCreatureReady(player1, new TwoHeadedHellkite());
        addCreatureReady(player1, new TwoHeadedHellkite());
        harness.setLibrary(player1, List.of(new TwoHeadedHellkite(), new TwoHeadedHellkite(),
                new TwoHeadedHellkite(), new TwoHeadedHellkite()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }
}
