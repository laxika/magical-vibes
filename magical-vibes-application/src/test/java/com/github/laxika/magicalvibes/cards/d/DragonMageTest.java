package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonMage.class, ScornfulEgotist.class})
class DragonMageTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes each player discard their hand and draw seven cards")
    void combatDamageWheelsBothHandsIntoSevenCards() {
        Permanent dragonMage = addCreatureReady(player1, new DragonMage());
        dragonMage.setAttacking(true);
        harness.setHand(player1, List.of(new ScornfulEgotist(), new ScornfulEgotist()));
        harness.setHand(player2, List.of(new ScornfulEgotist()));
        harness.setLibrary(player1, sevenScornfulEgotists());
        harness.setLibrary(player2, sevenScornfulEgotists());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("No combat damage means no discard or draw")
    void noCombatDamageDoesNotTrigger() {
        addCreatureReady(player1, new DragonMage());
        harness.setHand(player1, List.of(new ScornfulEgotist()));
        harness.setHand(player2, List.of(new ScornfulEgotist()));
        harness.setLibrary(player1, sevenScornfulEgotists());
        harness.setLibrary(player2, sevenScornfulEgotists());

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Combat damage still makes players draw seven cards with empty hands")
    void combatDamageDrawsSevenCardsEvenWhenHandsAreEmpty() {
        Permanent dragonMage = addCreatureReady(player1, new DragonMage());
        dragonMage.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, sevenScornfulEgotists());
        harness.setLibrary(player2, sevenScornfulEgotists());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private List<Card> sevenScornfulEgotists() {
        return List.of(
                new ScornfulEgotist(), new ScornfulEgotist(), new ScornfulEgotist(), new ScornfulEgotist(),
                new ScornfulEgotist(), new ScornfulEgotist(), new ScornfulEgotist());
    }
}
