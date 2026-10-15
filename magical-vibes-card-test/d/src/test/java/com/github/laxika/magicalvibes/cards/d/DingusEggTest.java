package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.a.Armageddon;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.Jokulhaups;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DingusEgg.class, Armageddon.class, Boomerang.class, DemonicHordes.class, GrizzlyBears.class,
        Jokulhaups.class, Mountain.class, Shatter.class, StoneRain.class, WrathOfGod.class})
class DingusEggTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to the land's controller when their land is destroyed")
    void dealsToLandControllerWhenLandDestroyed() {
        harness.addToBattlefield(player1, new DingusEgg());
        UUID mountainId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, mountainId); // Resolve Stone Rain — Mountain dies

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities(); // Resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Trigger still resolves after Dingus Egg leaves the battlefield")
    void triggerResolvesAfterEggLeavesBattlefield() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new DingusEgg());
        harness.addToBattlefield(player2, new Mountain());
        harness.setLife(player2, 20);

        UUID mountainId = harness.getPermanentId(player2, "Mountain");
        harness.setHand(player1, List.of(new StoneRain(), new Shatter()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, mountainId);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, egg.getId());

        harness.assertNotOnBattlefield(player1, "Dingus Egg");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Triggers when a land is sacrificed")
    void triggersWhenLandIsSacrificed() {
        harness.addToBattlefield(player1, new DingusEgg());
        addCreatureReady(player1, new DemonicHordes());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handlePermanentChosen(player2, land.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 2 damage to controller when their own land is destroyed")
    void dealsToSelfWhenOwnLandDestroyed() {
        harness.addToBattlefield(player1, new DingusEgg());
        UUID mountainId = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        harness.setLife(player1, 20);

        harness.setHand(player2, List.of(new StoneRain()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, mountainId); // Resolve Stone Rain

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // Resolve trigger

        // Player1 controlled the land, so player1 takes the 2 damage
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not trigger when a non-land permanent dies")
    void doesNotTriggerOnNonLand() {
        harness.addToBattlefield(player1, new DingusEgg());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities(); // Resolve Wrath of God - Grizzly Bears dies

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Two Dingus Eggs each trigger when a land is destroyed")
    void twoEggsEachTrigger() {
        harness.addToBattlefield(player1, new DingusEgg());
        harness.addToBattlefield(player1, new DingusEgg());
        UUID mountainId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, mountainId); // Resolve Stone Rain

        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();

        // 2 + 2 = 4 damage total
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Triggers once for each land destroyed by one effect")
    void triggersForEachLandDestroyedByOneEffect() {
        harness.addToBattlefield(player1, new DingusEgg());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new Armageddon(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Trigger is logged when it fires")
    void triggerIsLogged() {
        harness.addToBattlefield(player1, new DingusEgg());
        UUID mountainId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();

        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, mountainId);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("'s ability triggers."));
    }

    @Test
    @DisplayName("Triggers for every land destroyed simultaneously with Dingus Egg")
    void triggersWhenDestroyedSimultaneouslyWithLands() {
        harness.addToBattlefield(player1, new DingusEgg());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new Jokulhaups(), "{4}{R}{R}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dingus Egg");
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Returning a land to hand does not trigger Dingus Egg")
    void doesNotTriggerWhenLandReturnsToHand() {
        harness.addToBattlefield(player1, new DingusEgg());
        UUID landId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, landId);

        harness.assertInHand(player2, "Mountain");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not trigger for lands destroyed after Dingus Egg has left")
    void doesNotTriggerAfterEggLeaves() {
        UUID eggId = harness.addToBattlefieldAndReturn(player1, new DingusEgg()).getId();
        UUID landId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shatter(), new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, eggId);
        harness.castAndResolveSorcery(player1, 0, landId);

        harness.assertInGraveyard(player1, "Dingus Egg");
        harness.assertInGraveyard(player2, "Mountain");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }
}
