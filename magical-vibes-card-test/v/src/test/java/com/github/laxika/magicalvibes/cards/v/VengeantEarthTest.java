package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VengeantEarth.class, Forest.class, GrizzlyBears.class})
class VengeantEarthTest extends BaseCardTest {

    @Test
    @DisplayName("Turns a creature into a hasty 4/4 Elemental that must be blocked")
    void animatesCreatureAndRequiresBlockers() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castVengeantEarth(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target))
                .contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(target.isMustBeBlockedThisTurn()).isTrue();

        target.setSummoningSick(false);
        target.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");
    }

    @Test
    @DisplayName("Turns a controlled land into a 4/4 Elemental without removing its land type")
    void animatesControlledLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        castVengeantEarth(target);

        assertThat(gqs.isLand(gd, target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(target.isMustBeBlockedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Animation and combat requirement end at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castVengeantEarth(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(target.isMustBeBlockedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Cannot target an opponent's creature or land")
    void cannotTargetPermanentNotControlledByCaster() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new VengeantEarth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or land you control");
    }

    private void castVengeantEarth(Permanent target) {
        harness.setHand(player1, List.of(new VengeantEarth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    @Test
    void countersApplyOnTopOfNewBasePowerAndToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castVengeantEarth(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
    }

    @Test
    void retainsOriginalCreatureSubtype() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castVengeantEarth(target);

        assertThat(gqs.effectiveCreatureSubtypes(gd, target))
                .contains(CardSubtype.BEAR, CardSubtype.ELEMENTAL);
    }

    @Test
    void animatedLandCanAttackImmediatelyWithHaste() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        target.setSummoningSick(true);
        castVengeantEarth(target);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(target.isAttacking()).isTrue();
    }

    @Test
    void oneBlockerSatisfiesRequirementEvenWhenMoreAreAvailable() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        castVengeantEarth(target);
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    void mayRemainUnblockedWhenOnlyPotentialBlockerIsTapped() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.tap();
        castVengeantEarth(target);
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    void landStopsBeingCreatureAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        castVengeantEarth(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(target.isMustBeBlockedThisTurn()).isFalse();
    }

    @Test
    void targetChangingControllerBeforeResolutionIsUnaffected() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new VengeantEarth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(target.isMustBeBlockedThisTurn()).isFalse();
    }
}
