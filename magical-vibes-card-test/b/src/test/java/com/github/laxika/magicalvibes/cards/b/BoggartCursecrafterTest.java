package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SkirkProspector;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoggartCursecrafter.class, SkirkProspector.class, GrizzlyBears.class, Shock.class,
        LightningBolt.class, AmoeboidChangeling.class})
class BoggartCursecrafterTest extends BaseCardTest {

    @Test
    @DisplayName("Another Goblin dying deals 1 damage to each opponent")
    void goblinDeathDealsDamageToEachOpponent() {
        addCreatureReady(player1, new BoggartCursecrafter());
        harness.addToBattlefield(player1, new SkirkProspector());

        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        killCreature(player1, "Skirk Prospector");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    @DisplayName("A non-Goblin dying does not trigger Boggart Cursecrafter")
    void nonGoblinDeathDoesNotTrigger() {
        addCreatureReady(player1, new BoggartCursecrafter());
        harness.addToBattlefield(player1, new GrizzlyBears());

        int opponentLifeBefore = gd.getLife(player2.getId());

        killCreature(player1, "Grizzly Bears");

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Boggart Cursecrafter does not trigger for its own death")
    void ownDeathDoesNotTrigger() {
        addCreatureReady(player1, new BoggartCursecrafter());

        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Boggart Cursecrafter"));
        harness.assertInGraveyard(player1, "Boggart Cursecrafter");

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Goblin dying does not trigger")
    void opposingGoblinDeathDoesNotTrigger() {
        addCreatureReady(player1, new BoggartCursecrafter());
        harness.addToBattlefield(player2, new SkirkProspector());

        int opponentLifeBefore = gd.getLife(player2.getId());
        killCreature(player2, "Skirk Prospector");

        harness.assertInGraveyard(player2, "Skirk Prospector");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("A creature that gained all creature types triggers when it dies")
    void temporarilyGainedGoblinTypeTriggers() {
        addCreatureReady(player1, new BoggartCursecrafter());
        addCreatureReady(player1, new AmoeboidChangeling());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 1, 0, null, targetId);
        harness.passBothPriorities();

        int opponentLifeBefore = gd.getLife(player2.getId());
        killCreature(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    @DisplayName("A Goblin that lost all creature types does not trigger when it dies")
    void temporarilyLostGoblinTypeDoesNotTrigger() {
        addCreatureReady(player1, new BoggartCursecrafter());
        addCreatureReady(player1, new AmoeboidChangeling());
        harness.addToBattlefield(player1, new SkirkProspector());
        UUID targetId = harness.getPermanentId(player1, "Skirk Prospector");
        harness.activateAbility(player1, 1, 1, null, targetId);
        harness.passBothPriorities();

        int opponentLifeBefore = gd.getLife(player2.getId());
        killCreature(player1, "Skirk Prospector");

        harness.assertInGraveyard(player1, "Skirk Prospector");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("A death trigger still deals damage after Boggart Cursecrafter leaves")
    void triggerResolvesAfterSourceDies() {
        addCreatureReady(player1, new BoggartCursecrafter());
        harness.addToBattlefield(player1, new SkirkProspector());
        int opponentLifeBefore = gd.getLife(player2.getId());
        killCreature(player1, "Skirk Prospector");
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Boggart Cursecrafter"));
        harness.assertInGraveyard(player1, "Boggart Cursecrafter");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    @DisplayName("Deathtouch kills a blocker despite dealing less than its toughness")
    void deathtouchKillsBlocker() {
        addCreatureReady(player1, new BoggartCursecrafter());
        harness.addToBattlefield(player2, new BoggartCursecrafter());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Boggart Cursecrafter");
        harness.assertInGraveyard(player2, "Boggart Cursecrafter");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
    private void killCreature(Player controller, String targetName) {
        harness.setHand(controller, List.of(new Shock()));
        harness.addMana(controller, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(controller, targetName);
        harness.castAndResolveInstant(controller, 0, targetId);
    }
}
