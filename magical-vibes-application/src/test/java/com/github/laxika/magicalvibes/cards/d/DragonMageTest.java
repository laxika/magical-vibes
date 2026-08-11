package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DragonMageTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes each player discard their hand and draw seven cards")
    void combatDamageWheelsBothHandsIntoSevenCards() {
        Permanent dragonMage = addCreatureReady(player1, new DragonMage());
        dragonMage.setAttacking(true);
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player1, sevenIslands());
        harness.setLibrary(player2, sevenIslands());

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
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, sevenIslands());
        harness.setLibrary(player2, sevenIslands());

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private List<Card> sevenIslands() {
        return List.of(
                new Island(), new Island(), new Island(), new Island(),
                new Island(), new Island(), new Island());
    }
}
