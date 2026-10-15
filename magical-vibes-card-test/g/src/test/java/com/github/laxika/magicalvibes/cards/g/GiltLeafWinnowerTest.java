package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.e.ElvishArchers;
import com.github.laxika.magicalvibes.cards.h.HitchclawRecluse;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiltLeafWinnower.class, EliteVanguard.class, ElvishArchers.class, GrizzlyBears.class,
        HitchclawRecluse.class, TurnToFrog.class})
class GiltLeafWinnowerTest extends BaseCardTest {

    private void castWinnower() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GiltLeafWinnower()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting the ETB may destroys a non-Elf creature with unequal power and toughness")
    void destroysNonElfWithUnequalPowerAndToughness() {
        harness.addToBattlefield(player2, new EliteVanguard());
        UUID vanguardId = harness.getPermanentId(player2, "Elite Vanguard");

        castWinnower();
        harness.handlePermanentChosen(player1, vanguardId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Elite Vanguard");
        harness.assertOnBattlefield(player1, "Gilt-Leaf Winnower");
    }

    @Test
    @DisplayName("Declining the may leaves the creature alive")
    void decliningLeavesCreatureAlive() {
        harness.addToBattlefield(player2, new EliteVanguard());
        UUID vanguardId = harness.getPermanentId(player2, "Elite Vanguard");

        castWinnower();
        harness.handlePermanentChosen(player1, vanguardId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Elite Vanguard");
    }

    @Test
    @DisplayName("Elves and creatures with equal power and toughness are not legal targets")
    void elvesAndEqualStatsCreaturesAreIllegalTargets() {
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.addToBattlefield(player2, new ElvishArchers());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID archersId = harness.getPermanentId(player2, "Elvish Archers");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castWinnower();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).doesNotContain(archersId, bearsId);
        assertThat(choice.validPermanentIds()).contains(harness.getPermanentId(player2, "Elite Vanguard"));
    }

    @Test
    @DisplayName("No trigger goes on the stack when no legal target exists")
    void noTriggerWithoutLegalTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castWinnower();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Gilt-Leaf Winnower");
    }

    @Test
    void canDestroyOwnCreatureWithToughnessGreaterThanPower() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new HitchclawRecluse()).getId();

        castWinnower();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Hitchclaw Recluse");
        harness.assertOnBattlefield(player1, "Gilt-Leaf Winnower");
    }

    @Test
    void targetBecomingEqualPowerAndToughnessMakesAbilityFizzle() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new HitchclawRecluse()).getId();
        castWinnower();
        harness.handlePermanentChosen(player1, targetId);

        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hitchclaw Recluse");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotBeBlockedByOnlyOneCreature() {
        addCreatureReady(player1, new GiltLeafWinnower());
        addCreatureReady(player2, new HitchclawRecluse());
        addCreatureReady(player2, new HitchclawRecluse());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void canBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new GiltLeafWinnower());
        var first = addCreatureReady(player2, new HitchclawRecluse());
        var second = addCreatureReady(player2, new HitchclawRecluse());
        declareAttackersAndPrepareBlockers(List.of(0));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2,
                        List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    void defenderCanLeaveWinnowerUnblocked() {
        addCreatureReady(player1, new GiltLeafWinnower());
        addCreatureReady(player2, new HitchclawRecluse());
        addCreatureReady(player2, new HitchclawRecluse());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 16);
    }
}
