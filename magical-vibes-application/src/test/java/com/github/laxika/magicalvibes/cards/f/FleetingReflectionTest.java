package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FleetingReflection.class, GrizzlyBears.class, HillGiant.class})
class FleetingReflectionTest extends BaseCardTest {

    @Test
    @DisplayName("Gains hexproof, untaps, and copies the other target creature")
    void protectsUntapsAndCopies() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent copySource = addCreatureReady(player2, new HillGiant());
        target.tap();

        castFleetingReflection(target, copySource);

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
        assertThat(target.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("The optional copy target may be omitted")
    void copyTargetMayBeOmitted() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new FleetingReflection()));
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
        assertThat(target.getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("An illegal copy target does not stop the first target's other effects")
    void illegalCopyTargetStillProtectsAndUntapsFirstTarget() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent copySource = addCreatureReady(player2, new HillGiant());
        target.tap();

        harness.setHand(player1, List.of(new FleetingReflection()));
        addMana();
        harness.castInstant(player1, 0, List.of(target.getId(), copySource.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(copySource);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
        assertThat(target.getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Copy and hexproof expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent copySource = addCreatureReady(player2, new HillGiant());

        castFleetingReflection(target, copySource);
        assertThat(target.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("The first target must be a creature you control")
    void firstTargetMustBeControlledCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FleetingReflection()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("The copy source must be a different creature")
    void cannotCopyItself() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FleetingReflection()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different");
    }

    @Test
    @DisplayName("A copy source that gains hexproof in response cannot be copied")
    void copySourceGainingHexproofStillAllowsProtectionAndUntap() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent copySource = addCreatureReady(player2, new HillGiant());
        target.tap();
        harness.setHand(player1, List.of(new FleetingReflection()));
        addMana();
        harness.castInstant(player1, 0, List.of(target.getId(), copySource.getId()));

        harness.setHand(player2, List.of(new FleetingReflection()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, copySource.getId());
        assertThat(gqs.hasKeyword(gd, copySource, Keyword.HEXPROOF)).isTrue();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
        assertThat(target.getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("A first target no longer under your control receives none of the effects")
    void firstTargetChangingControllerIsUnaffected() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent copySource = addCreatureReady(player2, new HillGiant());
        target.tap();
        harness.setHand(player1, List.of(new FleetingReflection()));
        addMana();
        harness.castInstant(player1, 0, List.of(target.getId(), copySource.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
        assertThat(target.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(copySource.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(gqs.hasKeyword(gd, copySource, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Copying preserves the first creature's counters without copying the source's counters or tap state")
    void countersAndTapStateAreNotCopied() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent copySource = addCreatureReady(player2, new HillGiant());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        copySource.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        target.tap();
        copySource.tap();

        castFleetingReflection(target, copySource);

        assertThat(target.isTapped()).isFalse();
        assertThat(copySource.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, copySource)).isEqualTo(6);
    }

    @Test
    @DisplayName("Copying a creature that is already a copy uses its copied characteristics")
    void copiesAnExistingCopyAndRevertsToOriginalAtEndOfTurn() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent intermediate = addCreatureReady(player1, new GrizzlyBears());
        Permanent copySource = addCreatureReady(player2, new HillGiant());
        castFleetingReflection(intermediate, copySource);

        castFleetingReflection(target, intermediate);

        assertThat(target.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(intermediate.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
    }

    private void castFleetingReflection(Permanent target, Permanent copySource) {
        harness.setHand(player1, List.of(new FleetingReflection()));
        addMana();
        harness.castAndResolveInstant(player1, 0, List.of(target.getId(), copySource.getId()));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
