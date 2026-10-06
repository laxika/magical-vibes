package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoseRoomTreasurer.class, SakuraTribeElder.class})
class RoseRoomTreasurerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates Treasures on the first two resolutions, then pays X for reflexive damage")
    void createsTwoTreasuresThenDealsPaidDamage() {
        addCreatureReady(player1, new RoseRoomTreasurer());
        Permanent target = addCreatureReady(player2, new SakuraTribeElder());
        harness.addMana(player1, ManaColor.GREEN, 8);

        castCreatureAndResolveTrigger();
        castCreatureAndResolveTrigger();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);

        castCreatureAndResolveTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();

        harness.handleXValueChosen(player1, 2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("Does not trigger for Rose Room Treasurer entering under its controller's control")
    void doesNotTriggerForItsOwnEntry() {
        harness.castFromHand(player1, new RoseRoomTreasurer(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingZeroStillQueuesReflexiveAbility() {
        addCreatureReady(player1, new RoseRoomTreasurer());
        harness.addMana(player1, ManaColor.GREEN, 7);
        castCreatureAndResolveTrigger();
        castCreatureAndResolveTrigger();
        castCreatureAndResolveTrigger();

        harness.handleXValueChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    void opponentCreatureDoesNotTriggerAlliance() {
        addCreatureReady(player1, new RoseRoomTreasurer());
        harness.enterBattlefieldAndReturn(player2, new SakuraTribeElder());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void queuedEntriesCreateTreasuresOnTheirFirstTwoResolutions() {
        addCreatureReady(player1, new RoseRoomTreasurer());
        harness.enterBattlefieldAndReturn(player1, new SakuraTribeElder());
        harness.enterBattlefieldAndReturn(player1, new SakuraTribeElder());

        assertThat(gd.stack).hasSize(2);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void treasurersTrackTheirResolutionsSeparately() {
        addCreatureReady(player1, new RoseRoomTreasurer());
        addCreatureReady(player1, new RoseRoomTreasurer());
        harness.enterBattlefieldAndReturn(player1, new SakuraTribeElder());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);

        harness.enterBattlefieldAndReturn(player1, new SakuraTribeElder());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).hasSize(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void laterResolutionsDealPaidDamageToPlayerWithoutCreatingMoreTreasures() {
        addCreatureReady(player1, new RoseRoomTreasurer());
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.enterBattlefieldAndReturn(player1, new SakuraTribeElder());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new SakuraTribeElder());
        harness.passBothPriorities();

        for (int payment : List.of(2, 3)) {
            harness.enterBattlefieldAndReturn(player1, new SakuraTribeElder());
            harness.passBothPriorities();
            harness.handleXValueChosen(player1, payment);
            harness.handlePermanentChosen(player1, player2.getId());
            harness.passBothPriorities();
        }

        harness.assertLife(player2, 15);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void firstTwoResolutionsCreateTreasuresAgainOnTheNextTurn() {
        addCreatureReady(player1, new RoseRoomTreasurer());
        harness.enterBattlefieldAndReturn(player1, new SakuraTribeElder());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new SakuraTribeElder());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.enterBattlefieldAndReturn(player1, new SakuraTribeElder());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new SakuraTribeElder());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(4);
        assertThat(gd.stack).isEmpty();
    }

    private void castCreatureAndResolveTrigger() {
        castCreatureUntilTriggerResolves();
        harness.passBothPriorities();
    }

    private void castCreatureUntilTriggerResolves() {
        harness.setHand(player1, List.of(new SakuraTribeElder()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
