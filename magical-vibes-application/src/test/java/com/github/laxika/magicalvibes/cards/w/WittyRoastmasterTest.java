package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CitizensCrowbar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WittyRoastmaster.class, GrizzlyBears.class, Murder.class, CitizensCrowbar.class})
class WittyRoastmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature entering under your control deals 1 damage to each opponent")
    void anotherCreatureEnteringDealsDamageToEachOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new WittyRoastmaster());

        castGrizzlyBears(player1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent's creature entering does not trigger Witty Roastmaster")
    void opponentCreatureEnteringDoesNotTrigger() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new WittyRoastmaster());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castGrizzlyBears(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Witty Roastmaster does not trigger on its own entry")
    void ownEntryDoesNotTrigger() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new WittyRoastmaster()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A second Roastmaster triggers only the first, and later entries trigger both")
    void multipleRoastmastersTriggerIndependently() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new WittyRoastmaster());
        harness.setHand(player1, List.of(new WittyRoastmaster(), new WittyRoastmaster()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An entry trigger still deals damage after its Roastmaster is destroyed")
    void triggerResolvesAfterSourceIsDestroyed() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new WittyRoastmaster());
        var sourceId = harness.getPermanentId(player1, "Witty Roastmaster");
        harness.setHand(player1, List.of(new WittyRoastmaster()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, 20);

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, sourceId);

        harness.assertInGraveyard(player1, "Witty Roastmaster");
        harness.assertLife(player2, 20);
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature token triggers Roastmaster, but the equipment creating it does not")
    void tokenEntryTriggersButNoncreatureEntryDoesNot() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new WittyRoastmaster());
        harness.setHand(player1, List.of(new CitizensCrowbar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Citizen's Crowbar");
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Citizen");
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, 20);

        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    private void castGrizzlyBears(com.github.laxika.magicalvibes.model.Player player) {
        harness.setHand(player, List.of(new GrizzlyBears()));
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.castCreature(player, 0);
    }
}
