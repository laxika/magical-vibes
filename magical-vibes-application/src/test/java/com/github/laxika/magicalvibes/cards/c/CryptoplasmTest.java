package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.b.BlisterstickShaman;
import com.github.laxika.magicalvibes.cards.t.ThrunTheLastTroll;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cryptoplasm.class, GrizzlyBears.class, ProdigalPyromancer.class,
        BlisterstickShaman.class, ThrunTheLastTroll.class})
class CryptoplasmTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger presents target selection (not may prompt)")
    void upkeepTriggerPresentsTargetSelection() {
        addCreatureReady(player1, new Cryptoplasm());
        addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        // Target selection is presented immediately — no may prompt first
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Choosing target puts copy ability on the stack")
    void choosingTargetPutsAbilityOnStack() {
        addCreatureReady(player1, new Cryptoplasm());
        addCreatureReady(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bearsId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType())
                .isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId())
                .isEqualTo(bearsId);
    }

    @Test
    @DisplayName("Accepting may on resolution makes Cryptoplasm a copy of the target")
    void acceptingMayMakesCopy() {
        Permanent cryptoplasm = addCreatureReady(player1, new Cryptoplasm());
        addCreatureReady(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities(); // resolve → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept copy

        assertThat(cryptoplasm.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(cryptoplasm.getCard().getPower()).isEqualTo(2);
        assertThat(cryptoplasm.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining may on resolution does not change Cryptoplasm")
    void decliningMayDoesNotChangeCryptoplasm() {
        Permanent cryptoplasm = addCreatureReady(player1, new Cryptoplasm());
        addCreatureReady(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities(); // resolve → may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        assertThat(cryptoplasm.getCard().getName()).isEqualTo("Cryptoplasm");
    }

    @Test
    @DisplayName("Copy retains upkeep copy ability (except it has this ability)")
    void copyRetainsUpkeepCopyAbility() {
        addCreatureReady(player1, new Cryptoplasm());
        addCreatureReady(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bearsId);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Copy acquires target creature's activated abilities")
    void copyAcquiresTargetAbilities() {
        addCreatureReady(player1, new Cryptoplasm());
        addCreatureReady(player2, new ProdigalPyromancer());
        UUID pyromancerId = harness.getPermanentId(player2, "Prodigal Pyromancer");

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, pyromancerId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.setLife(player2, 20);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Can copy again on subsequent upkeep after becoming a copy")
    void canCopyAgainOnSubsequentUpkeep() {
        Permanent cryptoplasm = addCreatureReady(player1, new Cryptoplasm());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new ProdigalPyromancer());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        // First upkeep: copy Grizzly Bears
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(cryptoplasm.getCard().getName()).isEqualTo("Grizzly Bears");

        // Second upkeep: copy Prodigal Pyromancer
        UUID pyromancerId = harness.getPermanentId(player1, "Prodigal Pyromancer");
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, pyromancerId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(cryptoplasm.getCard().getName()).isEqualTo("Prodigal Pyromancer");
    }

    @Test
    @DisplayName("Trigger is removed from the stack when no legal target exists")
    void triggerIsRemovedWithNoOtherCreatures() {
        addCreatureReady(player1, new Cryptoplasm());
        // No other creatures on the battlefield

        advanceToUpkeep(player1);

        // Without a legal target, the triggered ability cannot remain on the stack.
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Can target opponent's creatures")
    void canTargetOpponentCreatures() {
        addCreatureReady(player1, new Cryptoplasm());
        addCreatureReady(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, bearsId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bearsId);
    }

    @Test
    @DisplayName("Copy effect fizzles if target is removed before resolution")
    void copyFizzlesIfTargetRemoved() {
        Permanent cryptoplasm = addCreatureReady(player1, new Cryptoplasm());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        UUID bearsId = bears.getId();

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bearsId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).remove(bears);

        harness.passBothPriorities(); // resolve — target gone, no may prompt queued

        // Cryptoplasm should remain unchanged
        assertThat(cryptoplasm.getCard().getName()).isEqualTo("Cryptoplasm");
    }

    @Test
    @DisplayName("Copying does not cause enters abilities to trigger")
    void copyingDoesNotTriggerEntersAbility() {
        Permanent cryptoplasm = addCreatureReady(player1, new Cryptoplasm());
        Permanent shaman = addCreatureReady(player2, new BlisterstickShaman());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, shaman.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(cryptoplasm.getCard().getName()).isEqualTo("Blisterstick Shaman");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Opponent's hexproof creature is not a legal upkeep target")
    void opponentHexproofCreatureIsNotLegalTarget() {
        Permanent cryptoplasm = addCreatureReady(player1, new Cryptoplasm());
        Permanent thrun = addCreatureReady(player2, new ThrunTheLastTroll());
        Permanent shaman = addCreatureReady(player2, new BlisterstickShaman());

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(shaman.getId())
                .doesNotContain(cryptoplasm.getId(), thrun.getId());
    }

    @Test
    @DisplayName("Each copy trigger still affects its source after an earlier trigger changes its copy")
    void multipleCopyTriggersStillFindSourceAfterFirstCopy() {
        Permanent cryptoplasm = addCreatureReady(player1, new Cryptoplasm());
        Permanent other = addCreatureReady(player2, new Cryptoplasm());
        Permanent shaman = addCreatureReady(player2, new BlisterstickShaman());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, other.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Copying another Cryptoplasm gives two instances of the upkeep ability.
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, shaman.getId());
        harness.handlePermanentChosen(player1, other.getId());
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(cryptoplasm.getCard().getName()).isEqualTo("Blisterstick Shaman");
    }
}
