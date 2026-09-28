package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({XyrisTheWrithingStorm.class, GrizzlyBears.class})
class XyrisTheWrithingStormTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Snake for each opponent draw after the first draw of their draw step")
    void createsSnakeForExtraDraws() {
        harness.addToBattlefield(player1, new XyrisTheWrithingStorm());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);

        draw(player2);
        assertThat(gd.stack).isEmpty();

        draw(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Snake")).hasSize(1);
    }

    @Test
    @DisplayName("Makes both the controller and damaged player draw combat damage amount")
    void bothPlayersDrawCombatDamageAmount() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player2, 20);

        Permanent xyris = addCreatureReady(player1, new XyrisTheWrithingStorm());
        xyris.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(findPermanents(player1, "Snake")).hasSize(3);
    }

    @Test
    @DisplayName("Combat damage draws the exact top cards for both players")
    void combatDamageDrawsEachPlayersTopCards() {
        Card player1Drawn1 = new GrizzlyBears();
        Card player1Drawn2 = new GrizzlyBears();
        Card player1Drawn3 = new GrizzlyBears();
        Card player2Drawn1 = new GrizzlyBears();
        Card player2Drawn2 = new GrizzlyBears();
        Card player2Drawn3 = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(player1Drawn1, player1Drawn2, player1Drawn3));
        harness.setLibrary(player2, List.of(player2Drawn1, player2Drawn2, player2Drawn3));
        harness.setLife(player2, 20);
        addCreatureReady(player1, new XyrisTheWrithingStorm());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(player1Drawn1, player1Drawn2, player1Drawn3);
        assertThat(gd.playerHands.get(player2.getId()))
                .containsExactly(player2Drawn1, player2Drawn2, player2Drawn3);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
