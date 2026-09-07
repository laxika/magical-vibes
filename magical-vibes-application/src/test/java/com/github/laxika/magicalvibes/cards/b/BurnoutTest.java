package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Burnout.class, GrizzlyBears.class, Unsummon.class, LightningBolt.class, LlanowarElves.class})
class BurnoutTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a blue instant spell")
    void countersBlueInstant() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        Unsummon unsummon = new Unsummon();
        harness.setHand(player1, List.of(unsummon));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.setHand(player2, List.of(new Burnout()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, unsummon.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Unsummon");
        // Unsummon never resolved, so the creature is still on the battlefield.
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Does nothing to a non-blue instant spell (it resolves)")
    void doesNothingToNonBlueInstant() {
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new Burnout()));
        harness.addMana(player2, ManaColor.RED, 2);

        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bolt.getId());
        harness.passBothPriorities(); // Burnout resolves, does not counter
        harness.passBothPriorities(); // Lightning Bolt resolves

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Schedules a draw for its controller at the next upkeep even when the counter does nothing")
    void schedulesDelayedDraw() {
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new Burnout()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bolt.getId());
        harness.passBothPriorities();

        List<DrawCardsAtNextUpkeep> scheduled = harness.getGameData().getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player2.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    void drawsAtNextUpkeep() {
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new Burnout()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bolt.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player2.getId()).size();
        int libraryBefore = gd.playerDecks.get(player2.getId()).size();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libraryBefore - 1);
    }

    @Test
    @DisplayName("Cannot target a non-instant spell")
    void cannotTargetNonInstantSpell() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new Burnout()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, elves.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
