package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DazzlingTheaterPropRoom;
import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IntrudingSoulrager.class, DazzlingTheaterPropRoom.class, Forest.class, ElvishVisionary.class})
class IntrudingSoulragerTest extends BaseCardTest {

    @Test
    void sacrificingARoomDealsDamageToEachOpponentAndDraws() {
        Permanent soulrager = addCreatureReady(player1, new IntrudingSoulrager());
        Permanent room = harness.addToBattlefieldAndReturn(player1, new DazzlingTheaterPropRoom());
        harness.setLibrary(player1, List.of(new ElvishVisionary()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int player1HandSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(soulrager.isTapped()).isTrue();
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandSize + 1);
        harness.assertInHand(player1, "Elvish Visionary");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(room);
    }

    @Test
    void promptsWhichRoomToSacrifice() {
        addCreatureReady(player1, new IntrudingSoulrager());
        harness.addToBattlefield(player1, new DazzlingTheaterPropRoom());
        Permanent secondRoom = harness.addToBattlefieldAndReturn(player1, new DazzlingTheaterPropRoom());
        harness.setLibrary(player1, List.of(new ElvishVisionary()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, secondRoom.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(secondRoom);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void cannotActivateWithoutARoomToSacrifice() {
        harness.addToBattlefield(player1, new IntrudingSoulrager());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
