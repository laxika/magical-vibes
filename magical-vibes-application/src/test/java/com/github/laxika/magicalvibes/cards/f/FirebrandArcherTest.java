package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.o.OpenFire;
import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.cards.h.HollowOne;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FirebrandArcher.class, Opt.class, GrizzlyBears.class, Manalith.class, OpenFire.class, HollowOne.class})
class FirebrandArcherTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell triggers 1 damage to each opponent")
    void noncreatureSpellTriggersDamage() {
        harness.addToBattlefield(player1, new FirebrandArcher());
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);

        GameData gd = harness.getGameData();
        // Opt on stack + triggered ability
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Firebrand Archer"));
    }

    @Test
    @DisplayName("Resolving the trigger deals 1 damage to opponent only")
    void triggerDealsDamageToOpponent() {
        harness.addToBattlefield(player1, new FirebrandArcher());
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());

        harness.castInstant(player1, 0);
        // Resolve the triggered ability (LIFO — trigger sits on top of Opt)
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 1);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new FirebrandArcher());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        // Only the creature spell should be on the stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger the Archer")
    void opponentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new FirebrandArcher());
        harness.setHand(player2, List.of(new OpenFire()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(harness.getGameData().stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A noncreature artifact triggers before entering the battlefield")
    void artifactSpellTriggersDamage() {
        harness.addToBattlefield(player1, new FirebrandArcher());
        harness.setHand(player1, List.of(new Manalith()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Manalith");
        assertThat(harness.getGameData().stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Manalith");
    }

    @Test
    @DisplayName("An artifact creature spell does not trigger")
    void artifactCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new FirebrandArcher());
        harness.setHand(player1, List.of(new HollowOne()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hollow One");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The trigger still deals damage after the Archer dies")
    void triggerSurvivesSourceRemoval() {
        harness.addToBattlefield(player1, new FirebrandArcher());
        harness.setHand(player1, List.of(new Manalith()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player2, List.of(new OpenFire()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castArtifact(player1, 0);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Firebrand Archer"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Firebrand Archer");
        harness.assertInGraveyard(player1, "Firebrand Archer");
        assertThat(harness.getGameData().stack).hasSize(2);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Each Archer independently triggers for the same spell")
    void multipleArchersEachTrigger() {
        harness.addToBattlefield(player1, new FirebrandArcher());
        harness.addToBattlefield(player1, new FirebrandArcher());
        harness.setHand(player1, List.of(new Manalith()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(3);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        assertThat(harness.getGameData().stack).hasSize(1);
    }
}
