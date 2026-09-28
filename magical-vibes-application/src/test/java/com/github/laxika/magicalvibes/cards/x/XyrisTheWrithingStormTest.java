package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({XyrisTheWrithingStorm.class, GrizzlyBears.class})
class XyrisTheWrithingStormTest extends BaseCardTest {

    @Test
    void createsSnakesForOpponentAdditionalDraws() {
        harness.addToBattlefield(player1, new XyrisTheWrithingStorm());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        draw(player2);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Snake")).isEmpty();

        draw(player2);
        resolveAllTriggers();
        draw(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Snake")).hasSize(2);
    }

    @Test
    void combatDamageMakesBothPlayersDrawThatMuch() {
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
