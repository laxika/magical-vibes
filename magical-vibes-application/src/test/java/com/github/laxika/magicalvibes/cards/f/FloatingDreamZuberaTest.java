package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.r.RendSpirit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FloatingDreamZubera.class, RendSpirit.class, LanternKami.class})
class FloatingDreamZuberaTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card for each Zubera that died this turn")
    void drawsForEachZuberaThatDiedThisTurn() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new FloatingDreamZubera());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new FloatingDreamZubera());
        Permanent nonZubera = harness.addToBattlefieldAndReturn(player2, new LanternKami());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new RendSpirit(), new RendSpirit(), new RendSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        int startingHand = gd.playerHands.get(player2.getId()).size();

        harness.castAndResolveInstant(player1, 0, first.getId());
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(startingHand + 1);

        harness.castAndResolveInstant(player1, 0, second.getId());
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(startingHand + 3);

        harness.castAndResolveInstant(player1, 0, nonZubera.getId());
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(startingHand + 3);
    }

    @Test
    @DisplayName("Counts Zubera deaths under either player's control")
    void countsZuberaDeathsUnderEitherPlayersControl() {
        Permanent ownZubera = harness.addToBattlefieldAndReturn(player1, new FloatingDreamZubera());
        Permanent opposingZubera = harness.addToBattlefieldAndReturn(player2, new FloatingDreamZubera());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new RendSpirit(), new RendSpirit()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        int startingHand = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveInstant(player2, 0, opposingZubera.getId());
        resolveAllTriggers();

        harness.castAndResolveInstant(player2, 0, ownZubera.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(startingHand + 2);
    }
}
