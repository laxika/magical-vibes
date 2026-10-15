package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DemonicTaskmaster.class, GrizzlyBears.class, GiantSpider.class, DeathWind.class, Cloudshift.class})
class DemonicTaskmasterTest extends BaseCardTest {

    // "At the beginning of your upkeep, sacrifice a creature other than this creature."

    @Test
    @DisplayName("Alone, does nothing — does not sacrifice itself")
    void aloneDoesNothing() {
        harness.addToBattlefield(player1, new DemonicTaskmaster());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Demonic Taskmaster");
    }

    @Test
    @DisplayName("Auto-sacrifices the only other creature")
    void autoSacrificesOnlyOtherCreature() {
        harness.addToBattlefield(player1, new DemonicTaskmaster());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Demonic Taskmaster");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("With multiple other creatures, controller chooses which to sacrifice")
    void controllerChoosesWhichOtherCreatureToSacrifice() {
        Permanent taskmaster = harness.addToBattlefieldAndReturn(player1, new DemonicTaskmaster());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(bears.getId(), spider.getId());
        assertThat(choice.validIds()).doesNotContain(taskmaster.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Giant Spider");
        harness.assertOnBattlefield(player1, "Demonic Taskmaster");
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new DemonicTaskmaster());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Demonic Taskmaster");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opponent's creatures cannot be sacrificed to the upkeep ability")
    void doesNotSacrificeOpponentsCreature() {
        harness.addToBattlefield(player1, new DemonicTaskmaster());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Demonic Taskmaster");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Two Taskmasters sacrifice each other as their upkeep triggers resolve")
    void twoTaskmastersSacrificeEachOther() {
        harness.addToBattlefield(player1, new DemonicTaskmaster());
        harness.addToBattlefield(player1, new DemonicTaskmaster());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Demonic Taskmaster")).isEqualTo(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Demonic Taskmaster");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Demonic Taskmaster"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Upkeep sacrifice still happens after the Taskmaster leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        Permanent taskmaster = harness.addToBattlefieldAndReturn(player1, new DemonicTaskmaster());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeathWind()));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, 3, taskmaster.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Demonic Taskmaster");
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A Taskmaster that leaves and returns is eligible for its old upkeep sacrifice")
    void returnedTaskmasterIsNotExcludedByOldTrigger() {
        Permanent taskmaster = harness.addToBattlefieldAndReturn(player1, new DemonicTaskmaster());
        harness.setHand(player1, List.of(new Cloudshift()));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, taskmaster.getId());
        assertThat(findPermanent(player1, "Demonic Taskmaster").getId())
                .isNotEqualTo(taskmaster.getId());
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Demonic Taskmaster");
        harness.assertInGraveyard(player1, "Demonic Taskmaster");
    }
}
