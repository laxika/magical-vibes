package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CovetedFalcon.class, GrizzlyBears.class})
class CovetedFalconTest extends BaseCardTest {

    @Test
    void turningFaceUpGivesOpponentAnyNumberOfPermanentsAndDrawsForEach() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent falcon = castFaceDown();
        int handSizeBeforeDraw = gd.playerHands.get(player1.getId()).size();

        turnFaceUp(falcon);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(first.getId(), second.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .doesNotContain(first.getId(), second.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeDraw + 2);
    }

    @Test
    void attacksToGainControlOfPermanentItsOwnerDoesNotControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent falcon = castFaceDown();
        turnFaceUp(falcon);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        falcon.setSummoningSick(false);

        int falconIndex = gd.playerBattlefields.get(player1.getId()).indexOf(falcon);
        declareAttackers(player1, List.of(falconIndex));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .contains(target.getId());
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new CovetedFalcon()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Coveted Falcon");
    }

    private void turnFaceUp(Permanent falcon) {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(falcon));
    }
}
