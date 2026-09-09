package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PaintersStudioDefacedGallery.class, GrizzlyBears.class})
class PaintersStudioDefacedGalleryTest extends BaseCardTest {

    @Test
    void painterStudioExilesTopTwoCardsAndAllowsPlayingThem() {
        Card first = new Card();
        Card second = new Card();
        harness.setLibrary(player1, List.of(first, second));

        castRoom(0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
    }

    @Test
    void defacedGalleryBoostsOnlyAttackingCreaturesYouControl() {
        Permanent room = harness.addToBattlefieldAndReturn(player1, new PaintersStudioDefacedGallery());
        room.unlockRoomDoor(1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
    }

    @Test
    void defacedGalleryBoostWearsOffAtEndOfTurn() {
        Permanent room = harness.addToBattlefieldAndReturn(player1, new PaintersStudioDefacedGallery());
        room.unlockRoomDoor(1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new PaintersStudioDefacedGallery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, doorIndex == 0 ? 2 : 1);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}
