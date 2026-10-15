package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.MoriokReaver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrossHopper.class, MoriokReaver.class})
class DrossHopperTest extends BaseCardTest {


    @Test
    @DisplayName("Sacrificing a creature grants Dross Hopper flying until end of turn")
    void sacrificeCreatureGrantsFlying() {
        Permanent hopper = addCreatureReady(player1, new DrossHopper());
        harness.addToBattlefield(player1, new MoriokReaver());
        UUID reaverId = harness.getPermanentId(player1, "Moriok Reaver");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, reaverId);
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        harness.passBothPriorities();

        // Reaver should be sacrificed
        harness.assertNotOnBattlefield(player1, "Moriok Reaver");
        harness.assertInGraveyard(player1, "Moriok Reaver");

        // Hopper should have flying
        assertThat(hopper.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Flying granted by ability resets at end of turn")
    void flyingResetsAtEndOfTurn() {
        Permanent hopper = addCreatureReady(player1, new DrossHopper());
        harness.addToBattlefield(player1, new MoriokReaver());
        UUID reaverId = harness.getPermanentId(player1, "Moriok Reaver");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, reaverId);
        harness.passBothPriorities();

        assertThat(hopper.getGrantedKeywords()).contains(Keyword.FLYING);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(hopper.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Can sacrifice Dross Hopper to its own ability (resolves without granting flying)")
    void canSacrificeItself() {
        addCreatureReady(player1, new DrossHopper());

        harness.activateAbility(player1, 0, null, null);

        // Hopper should be sacrificed, ability on stack
        harness.assertNotOnBattlefield(player1, "Dross Hopper");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        // The non-targeting ability resolves even though its source is gone.
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability has no mana cost and does not require tap")
    void noManaCostNoTapRequired() {
        Permanent hopper = addCreatureReady(player1, new DrossHopper());
        hopper.tap();
        harness.addToBattlefield(player1, new MoriokReaver());
        UUID reaverId = harness.getPermanentId(player1, "Moriok Reaver");

        // No mana added, hopper is tapped â€” should still work
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, reaverId);

        assertThat(gd.stack).hasSize(1);
    }


    @Test
    @DisplayName("Sacrifice is paid before resolution and only the activating Hopper gains flying")
    void sacrificeIsPaidBeforeFlyingIsGranted() {
        Permanent hopper = addCreatureReady(player1, new DrossHopper());
        Permanent otherHopper = addCreatureReady(player1, new DrossHopper());
        Permanent reaver = harness.addToBattlefieldAndReturn(player1, new MoriokReaver());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, reaver.getId());

        harness.assertNotOnBattlefield(player1, "Moriok Reaver");
        harness.assertInGraveyard(player1, "Moriok Reaver");
        assertThat(gd.stack).hasSize(1);
        assertThat(hopper.getGrantedKeywords()).doesNotContain(Keyword.FLYING);

        harness.passBothPriorities();

        assertThat(hopper.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(otherHopper.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("A summoning-sick Hopper can activate its ability")
    void summoningSicknessDoesNotPreventActivation() {
        Permanent hopper = harness.addToBattlefieldAndReturn(player1, new DrossHopper());
        hopper.setSummoningSick(true);
        Permanent reaver = harness.addToBattlefieldAndReturn(player1, new MoriokReaver());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, reaver.getId());
        harness.passBothPriorities();

        assertThat(hopper.getGrantedKeywords()).contains(Keyword.FLYING);
        harness.assertInGraveyard(player1, "Moriok Reaver");
    }
}
